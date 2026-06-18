resource "random_password" "db" {
  for_each = local.sql_databases

  length  = 32
  special = false
}

resource "google_sql_database_instance" "postgres" {
  name                = "${local.prefix}-postgres"
  database_version    = var.cloud_sql_database_version
  region              = var.region
  deletion_protection = var.deletion_protection

  settings {
    tier              = var.cloud_sql_tier
    availability_type = "ZONAL"
    disk_autoresize   = true
    disk_size         = var.cloud_sql_disk_size_gb
    disk_type         = "PD_SSD"
    user_labels       = local.common_labels

    backup_configuration {
      enabled                        = var.cloud_sql_backup_enabled
      point_in_time_recovery_enabled = false
    }

    ip_configuration {
      ipv4_enabled                                  = false
      private_network                               = google_compute_network.dev.id
      enable_private_path_for_google_cloud_services = true
    }
  }

  depends_on = [
    google_service_networking_connection.private_vpc_connection
  ]
}

resource "google_sql_database" "database" {
  for_each = local.sql_databases

  name     = each.value.database
  instance = google_sql_database_instance.postgres.name
}

resource "google_sql_user" "service" {
  for_each = local.sql_databases

  name     = each.value.username
  instance = google_sql_database_instance.postgres.name
  password = random_password.db[each.key].result
}
