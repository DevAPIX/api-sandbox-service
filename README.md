# DevAPIX - Sandbox Service

## Overview
The Sandbox Service is a specialized environment within DevAPIX designed for API testing, evaluation, and rate limiting enforcement. It allows users to test endpoints securely and ensures that usage complies with active subscription tiers.

## Key Features
- **Private API "Hidden Link"**: Provides secure, obscure links for testing APIs before public release.
- **Subscription Limits Enforcement**: Tracks and limits API requests according to the user's subscription plan, returning HTTP 429 (Too Many Requests) when limits are exceeded.
- **Request Proxying**: Safely proxies requests to the actual backend APIs while maintaining analytics and usage tracking.
- **Isolated Testing**: Ensures that test requests do not interfere with production API usage metrics unless explicitly designed to do so.

## Technology Stack
- **Framework**: Spring Boot 3
- **Database**: PostgreSQL (for tracking usage data)
- **Service Discovery**: Netflix Eureka Client
