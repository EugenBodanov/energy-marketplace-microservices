resource "random_password" "jwt_secret" {
  length  = 64
  special = false
}

locals {
  runtime_secret_values = {
    "app-jwt-secret" = random_password.jwt_secret.result

    "user-db-url"      = "jdbc:postgresql://${google_sql_database_instance.postgres.private_ip_address}:5432/${google_sql_database.database["user"].name}"
    "user-db-username" = google_sql_user.service["user"].name
    "user-db-password" = random_password.db["user"].result

    "listing-db-url"      = "jdbc:postgresql://${google_sql_database_instance.postgres.private_ip_address}:5432/${google_sql_database.database["listing"].name}"
    "listing-db-username" = google_sql_user.service["listing"].name
    "listing-db-password" = random_password.db["listing"].result

    "trade-db-url"      = "jdbc:postgresql://${google_sql_database_instance.postgres.private_ip_address}:5432/${google_sql_database.database["trade"].name}"
    "trade-db-username" = google_sql_user.service["trade"].name
    "trade-db-password" = random_password.db["trade"].result

    "billing-db-host"     = google_sql_database_instance.postgres.private_ip_address
    "billing-db-port"     = "5432"
    "billing-db-name"     = google_sql_database.database["billing"].name
    "billing-db-user"     = google_sql_user.service["billing"].name
    "billing-db-password" = random_password.db["billing"].result

    "rabbitmq-host"     = google_compute_instance.rabbitmq.network_interface[0].network_ip
    "rabbitmq-port"     = "5672"
    "rabbitmq-username" = var.rabbitmq_username
    "rabbitmq-password" = random_password.rabbitmq.result
  }
}

resource "google_secret_manager_secret" "runtime" {
  for_each = local.runtime_secret_ids

  secret_id = "${local.secret_prefix}-${each.key}"
  labels    = local.common_labels

  replication {
    auto {}
  }
}

resource "google_secret_manager_secret_version" "runtime" {
  for_each = local.runtime_secret_values

  secret      = google_secret_manager_secret.runtime[each.key].id
  secret_data = each.value
}

resource "google_secret_manager_secret_iam_member" "runtime_access" {
  for_each = local.runtime_secret_access

  project   = var.project_id
  secret_id = google_secret_manager_secret.runtime[each.value.secret_id].secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${local.runtime_service_account_emails[each.value.service_name]}"
}
