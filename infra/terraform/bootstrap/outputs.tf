output "artifact_registry_repository" {
  description = "Artifact Registry Docker repository resource name."
  value       = google_artifact_registry_repository.docker.name
}

output "artifact_registry_image_prefix" {
  description = "Prefix used by CI when tagging Docker images."
  value       = "${var.artifact_registry_location}-docker.pkg.dev/${var.project_id}/${var.artifact_registry_repository}"
}

output "deploy_service_account_email" {
  description = "Service account email for GitHub Actions."
  value       = google_service_account.deployer.email
}

output "workload_identity_provider" {
  description = "Value for GitHub secret GCP_WORKLOAD_IDENTITY_PROVIDER."
  value       = google_iam_workload_identity_pool_provider.github.name
}

output "runtime_service_accounts" {
  description = "Runtime service accounts by Cloud Run service."
  value = {
    for service_name, service_account in google_service_account.runtime :
    service_name => service_account.email
  }
}
