resource "google_artifact_registry_repository" "docker" {
  location      = var.artifact_registry_location
  repository_id = var.artifact_registry_repository
  description   = "Docker images for Energy Marketplace microservices"
  format        = "DOCKER"
  labels        = local.common_labels

  depends_on = [
    google_project_service.required["artifactregistry.googleapis.com"]
  ]
}
