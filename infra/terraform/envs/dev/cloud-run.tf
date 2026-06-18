resource "google_cloud_run_v2_service" "user" {
  name                = "user-service"
  location            = var.region
  ingress             = var.cloud_run_ingress
  deletion_protection = var.deletion_protection
  labels              = local.common_labels

  template {
    service_account = local.runtime_service_account_emails["user-service"]

    scaling {
      min_instance_count = var.min_instance_count
      max_instance_count = var.max_instance_count
    }

    vpc_access {
      network_interfaces {
        network    = google_compute_network.dev.name
        subnetwork = google_compute_subnetwork.dev.name
      }
      egress = "PRIVATE_RANGES_ONLY"
    }

    containers {
      image = lookup(var.service_images, "user-service", var.placeholder_image)

      ports {
        container_port = 8080
      }

      resources {
        limits = {
          cpu    = "1"
          memory = "768Mi"
        }
        cpu_idle          = true
        startup_cpu_boost = true
      }

      env {
        name  = "SERVER_PORT"
        value = "8080"
      }

      env {
        name  = "APP_JWT_ISSUER"
        value = "user-service"
      }

      env {
        name  = "APP_JWT_EXPIRATION_MINUTES"
        value = tostring(var.jwt_expiration_minutes)
      }

      dynamic "env" {
        for_each = local.service_secret_env["user-service"]

        content {
          name = env.key
          value_source {
            secret_key_ref {
              secret  = google_secret_manager_secret.runtime[env.value].secret_id
              version = "latest"
            }
          }
        }
      }
    }
  }

  traffic {
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
    percent = 100
  }

  depends_on = [
    google_secret_manager_secret_iam_member.runtime_access,
    google_secret_manager_secret_version.runtime
  ]
}

resource "google_cloud_run_v2_service" "listing" {
  name                = "listing-service"
  location            = var.region
  ingress             = var.cloud_run_ingress
  deletion_protection = var.deletion_protection
  labels              = local.common_labels

  template {
    service_account = local.runtime_service_account_emails["listing-service"]

    scaling {
      min_instance_count = var.min_instance_count
      max_instance_count = var.max_instance_count
    }

    vpc_access {
      network_interfaces {
        network    = google_compute_network.dev.name
        subnetwork = google_compute_subnetwork.dev.name
      }
      egress = "PRIVATE_RANGES_ONLY"
    }

    containers {
      image = lookup(var.service_images, "listing-service", var.placeholder_image)

      ports {
        container_port = 8082
      }

      resources {
        limits = {
          cpu    = "1"
          memory = "768Mi"
        }
        cpu_idle          = true
        startup_cpu_boost = true
      }

      env {
        name  = "SERVER_PORT"
        value = "8082"
      }

      dynamic "env" {
        for_each = local.service_secret_env["listing-service"]

        content {
          name = env.key
          value_source {
            secret_key_ref {
              secret  = google_secret_manager_secret.runtime[env.value].secret_id
              version = "latest"
            }
          }
        }
      }
    }
  }

  traffic {
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
    percent = 100
  }

  depends_on = [
    google_secret_manager_secret_iam_member.runtime_access,
    google_secret_manager_secret_version.runtime,
    time_sleep.wait_for_rabbitmq
  ]
}

resource "google_cloud_run_v2_service" "billing" {
  name                = "billing-service"
  location            = var.region
  ingress             = var.cloud_run_ingress
  deletion_protection = var.deletion_protection
  labels              = local.common_labels

  template {
    service_account = local.runtime_service_account_emails["billing-service"]

    scaling {
      min_instance_count = var.min_instance_count
      max_instance_count = var.max_instance_count
    }

    vpc_access {
      network_interfaces {
        network    = google_compute_network.dev.name
        subnetwork = google_compute_subnetwork.dev.name
      }
      egress = "PRIVATE_RANGES_ONLY"
    }

    containers {
      image = lookup(var.service_images, "billing-service", var.placeholder_image)

      ports {
        container_port = 8000
      }

      resources {
        limits = {
          cpu    = "1"
          memory = "512Mi"
        }
        cpu_idle          = true
        startup_cpu_boost = true
      }

      dynamic "env" {
        for_each = local.service_secret_env["billing-service"]

        content {
          name = env.key
          value_source {
            secret_key_ref {
              secret  = google_secret_manager_secret.runtime[env.value].secret_id
              version = "latest"
            }
          }
        }
      }
    }
  }

  traffic {
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
    percent = 100
  }

  depends_on = [
    google_secret_manager_secret_iam_member.runtime_access,
    google_secret_manager_secret_version.runtime,
    time_sleep.wait_for_rabbitmq
  ]
}

resource "google_cloud_run_v2_service" "trade" {
  name                = "trade-service"
  location            = var.region
  ingress             = var.cloud_run_ingress
  deletion_protection = var.deletion_protection
  labels              = local.common_labels

  template {
    service_account = local.runtime_service_account_emails["trade-service"]

    scaling {
      min_instance_count = var.min_instance_count
      max_instance_count = var.max_instance_count
    }

    vpc_access {
      network_interfaces {
        network    = google_compute_network.dev.name
        subnetwork = google_compute_subnetwork.dev.name
      }
      egress = "PRIVATE_RANGES_ONLY"
    }

    containers {
      image = lookup(var.service_images, "trade-service", var.placeholder_image)

      ports {
        container_port = 8081
      }

      resources {
        limits = {
          cpu    = "1"
          memory = "768Mi"
        }
        cpu_idle          = true
        startup_cpu_boost = true
      }

      env {
        name  = "SERVER_PORT"
        value = "8081"
      }

      env {
        name  = "USER_SERVICE_URL"
        value = google_cloud_run_v2_service.user.uri
      }

      env {
        name  = "BILLING_SERVICE_BASE_URL"
        value = google_cloud_run_v2_service.billing.uri
      }

      dynamic "env" {
        for_each = local.service_secret_env["trade-service"]

        content {
          name = env.key
          value_source {
            secret_key_ref {
              secret  = google_secret_manager_secret.runtime[env.value].secret_id
              version = "latest"
            }
          }
        }
      }
    }
  }

  traffic {
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
    percent = 100
  }

  depends_on = [
    google_cloud_run_v2_service.billing,
    google_cloud_run_v2_service.user,
    google_secret_manager_secret_iam_member.runtime_access,
    google_secret_manager_secret_version.runtime,
    time_sleep.wait_for_rabbitmq
  ]
}

resource "google_cloud_run_v2_service" "api_gateway" {
  name                = "api-gateway"
  location            = var.region
  ingress             = var.cloud_run_ingress
  deletion_protection = var.deletion_protection
  labels              = local.common_labels

  template {
    service_account = local.runtime_service_account_emails["api-gateway"]

    scaling {
      min_instance_count = var.min_instance_count
      max_instance_count = var.max_instance_count
    }

    vpc_access {
      network_interfaces {
        network    = google_compute_network.dev.name
        subnetwork = google_compute_subnetwork.dev.name
      }
      egress = "PRIVATE_RANGES_ONLY"
    }

    containers {
      image = lookup(var.service_images, "api-gateway", var.placeholder_image)

      ports {
        container_port = 8000
      }

      resources {
        limits = {
          cpu    = "1"
          memory = "512Mi"
        }
        cpu_idle          = true
        startup_cpu_boost = true
      }

      env {
        name  = "SERVER_PORT"
        value = "8000"
      }

      env {
        name  = "USER_SERVICE_URL"
        value = google_cloud_run_v2_service.user.uri
      }

      env {
        name  = "LISTING_SERVICE_URL"
        value = google_cloud_run_v2_service.listing.uri
      }

      env {
        name  = "TRADE_SERVICE_URL"
        value = google_cloud_run_v2_service.trade.uri
      }
    }
  }

  traffic {
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
    percent = 100
  }

  depends_on = [
    google_cloud_run_v2_service.listing,
    google_cloud_run_v2_service.trade,
    google_cloud_run_v2_service.user
  ]
}
