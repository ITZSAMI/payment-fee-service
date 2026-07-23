output "invoice_bucket_name" {
  description = "Name of the S3 bucket used for invoice storage"
  value       = aws_s3_bucket.invoice_storage.bucket
}

output "ecr_repository_url" {
  description = "URL of the ECR repository for the app image"
  value       = aws_ecr_repository.payment_fee_service.repository_url
}
