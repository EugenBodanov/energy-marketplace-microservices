resource "random_password" "rabbitmq" {
  length  = 32
  special = false
}

resource "google_compute_disk" "rabbitmq_data" {
  name = "${local.prefix}-rabbitmq-data"
  type = "pd-balanced"
  zone = var.zone
  size = var.rabbitmq_disk_size_gb

  labels = local.common_labels
}

resource "google_compute_instance" "rabbitmq" {
  name         = "${local.prefix}-rabbitmq"
  machine_type = var.rabbitmq_machine_type
  zone         = var.zone
  tags         = [local.rabbitmq_network_tag]

  allow_stopping_for_update = true
  deletion_protection       = var.deletion_protection

  labels = local.common_labels

  boot_disk {
    initialize_params {
      image = "projects/debian-cloud/global/images/family/debian-12"
      size  = 10
      type  = "pd-balanced"
    }
  }

  attached_disk {
    source      = google_compute_disk.rabbitmq_data.id
    device_name = "rabbitmq-data"
    mode        = "READ_WRITE"
  }

  network_interface {
    subnetwork = google_compute_subnetwork.dev.id
  }

  metadata = {
    "enable-oslogin" = "TRUE"
  }

  metadata_startup_script = <<-EOT
#!/bin/bash
set -euo pipefail

if ! command -v docker >/dev/null 2>&1; then
  apt-get update
  apt-get install -y docker.io
fi

systemctl enable --now docker

mkdir -p /var/lib/rabbitmq

if ! blkid /dev/disk/by-id/google-rabbitmq-data >/dev/null 2>&1; then
  mkfs.ext4 -F /dev/disk/by-id/google-rabbitmq-data
fi

if ! grep -q "/var/lib/rabbitmq" /etc/fstab; then
  echo "/dev/disk/by-id/google-rabbitmq-data /var/lib/rabbitmq ext4 discard,defaults,nofail 0 2" >> /etc/fstab
fi

mount -a
chown -R 999:999 /var/lib/rabbitmq
chmod 700 /var/lib/rabbitmq

docker pull ${var.rabbitmq_image}
docker rm -f rabbitmq || true
docker run -d --name rabbitmq --restart unless-stopped \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER='${var.rabbitmq_username}' \
  -e RABBITMQ_DEFAULT_PASS='${random_password.rabbitmq.result}' \
  -v /var/lib/rabbitmq:/var/lib/rabbitmq \
  ${var.rabbitmq_image}
EOT

  service_account {
    scopes = [
      "https://www.googleapis.com/auth/logging.write",
      "https://www.googleapis.com/auth/monitoring.write"
    ]
  }

  depends_on = [
    google_compute_router_nat.dev
  ]
}

resource "time_sleep" "wait_for_rabbitmq" {
  create_duration = var.rabbitmq_bootstrap_wait

  depends_on = [
    google_compute_instance.rabbitmq
  ]
}
