terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# Bucket for storing generated invoice documents (e.g. PDFs).
# In a real system this is where InvoiceService would upload rendered invoices.
resource "aws_s3_bucket" "invoice_storage" {
  bucket = "${var.project_name}-invoices-${var.environment}"

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}

resource "aws_s3_bucket_versioning" "invoice_storage_versioning" {
  bucket = aws_s3_bucket.invoice_storage.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_public_access_block" "invoice_storage_block" {
  bucket = aws_s3_bucket.invoice_storage.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Container registry for the payment-fee-service Docker image.
resource "aws_ecr_repository" "payment_fee_service" {
  name                 = var.project_name
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = {
    Project     = var.project_name
    Environment = var.environment
  }
}
