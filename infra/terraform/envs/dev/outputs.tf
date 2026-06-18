output "cloud_run_urls" {
  description = "Cloud Run service URLs."
  value = {
    api_gateway = google_cloud_run_v2_service.api_gateway.uri
    user        = google_cloud_run_v2_service.user.uri
    listing     = google_cloud_run_v2_service.listing.uri
    trade       = google_cloud_run_v2_service.trade.uri
    billing     = google_cloud_run_v2_service.billing.uri
  }
}

output "rabbitmq_vm" {
  description = "RabbitMQ VM details."
  value = {
    name        = google_compute_instance.rabbitmq.name
    zone        = google_compute_instance.rabbitmq.zone
    internal_ip = google_compute_instance.rabbitmq.network_interface[0].network_ip
  }
}

output "cloud_sql" {
  description = "Cloud SQL instance details."
  value = {
    instance_name = google_sql_database_instance.postgres.name
    private_ip    = google_sql_database_instance.postgres.private_ip_address
    databases = {
      for key, database in google_sql_database.database : key => database.name
    }
  }
}

output "runtime_secret_ids" {
  description = "Secret Manager secret IDs created for runtime configuration."
  value = {
    for key, secret in google_secret_manager_secret.runtime : key => secret.secret_id
  }
}
