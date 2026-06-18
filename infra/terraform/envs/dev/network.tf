resource "google_compute_network" "dev" {
  name                    = "${local.prefix}-network"
  auto_create_subnetworks = false
  routing_mode            = "REGIONAL"
}

resource "google_compute_subnetwork" "dev" {
  name                     = "${local.prefix}-subnet"
  ip_cidr_range            = var.network_cidr
  region                   = var.region
  network                  = google_compute_network.dev.id
  private_ip_google_access = true
}

resource "google_compute_router" "dev" {
  name    = "${local.prefix}-router"
  region  = var.region
  network = google_compute_network.dev.id
}

resource "google_compute_router_nat" "dev" {
  name                               = "${local.prefix}-nat"
  router                             = google_compute_router.dev.name
  region                             = var.region
  nat_ip_allocate_option             = "AUTO_ONLY"
  source_subnetwork_ip_ranges_to_nat = "LIST_OF_SUBNETWORKS"

  subnetwork {
    name                    = google_compute_subnetwork.dev.id
    source_ip_ranges_to_nat = ["ALL_IP_RANGES"]
  }
}

resource "google_compute_global_address" "private_service_range" {
  name          = "${local.prefix}-google-managed-services"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = 16
  network       = google_compute_network.dev.id
}

resource "google_service_networking_connection" "private_vpc_connection" {
  network                 = google_compute_network.dev.id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_service_range.name]
}

resource "google_compute_firewall" "rabbitmq_amqp" {
  name    = "${local.prefix}-allow-rabbitmq-amqp"
  network = google_compute_network.dev.name

  direction     = "INGRESS"
  source_ranges = [var.network_cidr]
  target_tags   = [local.rabbitmq_network_tag]

  allow {
    protocol = "tcp"
    ports    = ["5672"]
  }
}

resource "google_compute_firewall" "rabbitmq_iap_ssh" {
  count = var.allow_iap_ssh ? 1 : 0

  name    = "${local.prefix}-allow-rabbitmq-iap-ssh"
  network = google_compute_network.dev.name

  direction     = "INGRESS"
  source_ranges = ["35.235.240.0/20"]
  target_tags   = [local.rabbitmq_network_tag]

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }
}
