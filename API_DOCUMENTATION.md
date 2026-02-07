# Pluta Camera AI - API Documentation

## Overview

The Pluta Camera AI API provides comprehensive endpoints for managing camera streams, processing videos, and performing AI-powered image analysis using YOLO object detection.

## Base URL

```
Local Development: http://localhost:8081/api
Production: https://api.pluta.com/api
```

## Authentication

All API endpoints require authentication using OAuth2/JWT tokens via Keycloak.

### Getting a Token

1. Authenticate with Keycloak
2. Include the JWT token in all requests:

```http
Authorization: Bearer {your-jwt-token}
```

### Required Roles

- `ADMIN`: Full access to all endpoints
- `MANAGER`: Access to most endpoints except system configuration
- `USER`: Read-only access to authorized resources

## Quick Start

### 1. Upload a Video for Processing

```bash
curl -X POST http://localhost:8081/api/v1/video/upload \
  -H "Authorization: Bearer {token}" \
  -F "file=@/path/to/video.mp4" \
  -F "zoneId=1" \
  -F "cameraId=1"
```

**Response:**
```json
{
  "success": true,
  "message": "Video uploaded successfully and processing started",
  "videoId": 123
}
```

### 2. Check Video Processing Status

```bash
curl -X GET http://localhost:8081/api/v1/video/123/status \
  -H "Authorization: Bearer {token}"
```

**Response:**
```json
{
  "status": "COMPLETED"
}
```

### 3. Create a Camera Stream

```bash
curl -X POST http://localhost:8081/api/v1/streams \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "cameraId": 1,
    "zoneId": 1,
    "branchId": 1,
    "tenantId": 1,
    "url": "rtsp://camera.example.com/stream1",
    "samplingIntervalSeconds": 30,
    "active": true
  }'
```

## API Endpoints

### Video Management

#### Upload Video
- **POST** `/v1/video/upload`
- **Description**: Upload a video file for AI processing
- **Content-Type**: `multipart/form-data`
- **Parameters**:
  - `file` (required): Video file
  - `zoneId` (required): Zone ID
  - `cameraId` (required): Camera ID
- **Response**: Video upload confirmation with video ID

#### Get All Videos
- **GET** `/v1/video`
- **Description**: Retrieve paginated list of all videos
- **Query Parameters**:
  - `page` (default: 0): Page number
  - `size` (default: 20): Items per page
  - `sort` (default: id,desc): Sort criteria
- **Response**: Array of video objects

#### Get Video by ID
- **GET** `/v1/video/{videoId}`
- **Description**: Get detailed information about a specific video
- **Path Parameters**:
  - `videoId`: Video ID
- **Response**: Video details object

#### Get Video Status
- **GET** `/v1/video/{videoId}/status`
- **Description**: Check processing status of a video
- **Path Parameters**:
  - `videoId`: Video ID
- **Response**: Status object
- **Status Values**:
  - `PENDING`: Waiting to be processed
  - `PROCESSING`: Currently being processed
  - `COMPLETED`: Processing finished successfully
  - `FAILED`: Processing failed

### Stream Management

#### Create Stream
- **POST** `/v1/streams`
- **Description**: Create a new camera stream configuration
- **Request Body**:
```json
{
  "cameraId": 1,
  "zoneId": 1,
  "branchId": 1,
  "tenantId": 1,
  "url": "rtsp://camera.example.com/stream1",
  "username": "admin",
  "password": "secret",
  "modelVersion": "yolo11x",
  "samplingIntervalSeconds": 30,
  "active": true
}
```
- **Response**: Created stream object

#### Get All Streams
- **GET** `/v1/streams`
- **Description**: Retrieve paginated list of all streams
- **Query Parameters**: Same as video endpoints
- **Response**: Paginated stream objects

#### Get Stream by ID
- **GET** `/v1/streams/{id}`
- **Path Parameters**:
  - `id`: Stream ID
- **Response**: Stream details object

#### Update Stream
- **PUT** `/v1/streams/{id}`
- **Description**: Update stream configuration
- **Path Parameters**:
  - `id`: Stream ID
- **Request Body**: Stream object (same as create)
- **Response**: Updated stream object

#### Activate Stream
- **PATCH** `/v1/streams/{id}/activate`
- **Description**: Activate a stream
- **Path Parameters**:
  - `id`: Stream ID
- **Response**: Updated stream object

#### Deactivate Stream
- **PATCH** `/v1/streams/{id}/deactivate`
- **Description**: Deactivate a stream
- **Path Parameters**:
  - `id`: Stream ID
- **Response**: Updated stream object

#### Delete Stream
- **DELETE** `/v1/streams/{id}`
- **Description**: Remove a stream
- **Path Parameters**:
  - `id`: Stream ID
- **Response**: 204 No Content

### Camera Management

#### Create Camera
- **POST** `/v1/cameras`
- **Description**: Register a new camera
- **Request Body**:
```json
{
  "name": "Main Entrance Camera",
  "code": "CAM-001",
  "zoneId": 1,
  "location": "Building A, Floor 1",
  "streamUrl": "rtsp://camera.example.com/stream1",
  "active": true
}
```

#### Get All Cameras
- **GET** `/v1/cameras`
- **Description**: Retrieve paginated list of cameras
- **Query Parameters**: page, size, sort
- **Response**: Paginated camera objects

#### Get Camera by ID
- **GET** `/v1/cameras/{id}`
- **Path Parameters**:
  - `id`: Camera ID
- **Response**: Camera details object

#### Update Camera
- **PUT** `/v1/cameras/{id}`
- **Description**: Update camera configuration
- **Path Parameters**:
  - `id`: Camera ID
- **Request Body**: Camera object
- **Response**: Updated camera object

#### Delete Camera
- **DELETE** `/v1/cameras/{id}`
- **Path Parameters**:
  - `id`: Camera ID
- **Response**: 204 No Content

#### Get Cameras by Zone
- **GET** `/v1/cameras/zone/{zoneId}`
- **Path Parameters**:
  - `zoneId`: Zone ID
- **Response**: Array of camera objects

### Frame Analysis

#### Analyze Image Frame
- **POST** `/frames/zone/{zoneId}/camera/{cameraId}/analyze`
- **Description**: Upload and analyze a single image frame
- **Content-Type**: `multipart/form-data`
- **Path Parameters**:
  - `zoneId`: Zone ID
  - `cameraId`: Camera ID
- **Form Parameters**:
  - `image` (required): Image file
  - `confidenceThreshold` (default: 0.2): Detection confidence threshold (0.0-1.0)
  - `zoneConfidenceThreshold` (default: 0.7): Zone confidence threshold (0.0-1.0)
- **Response**: Array of frame analysis results

```json
[
  {
    "tableId": 1,
    "detectedObjects": [
      {
        "class": "person",
        "confidence": 0.95,
        "bbox": [100, 150, 200, 300]
      }
    ],
    "inZone": true,
    "timestamp": "2024-01-15T10:30:00Z"
  }
]
```

## Response Schemas

### Video Response

```json
{
  "id": 123,
  "fileName": "video_20240115.mp4",
  "filePath": "/storage/videos/video_20240115.mp4",
  "duration": 120,
  "status": "COMPLETED",
  "framesExtracted": 60,
  "processingStartedAt": "2024-01-15T10:00:00Z",
  "processingCompletedAt": "2024-01-15T10:05:00Z",
  "errorMessage": null,
  "zoneId": 1,
  "cameraId": 1,
  "createdAt": "2024-01-15T09:55:00Z"
}
```

### Stream Response

```json
{
  "id": 1,
  "cameraId": 1,
  "cameraCode": "CAM-001",
  "zoneId": 1,
  "zoneCode": "ZONE-A",
  "branchId": 1,
  "branchCode": "BR-001",
  "tenantId": 1,
  "tenantCode": "TNT-001",
  "url": "rtsp://camera.example.com/stream1",
  "username": "admin",
  "modelVersion": "yolo11x",
  "samplingIntervalSeconds": 30,
  "active": true,
  "createdAt": "2024-01-15T09:00:00Z",
  "updatedAt": "2024-01-15T10:00:00Z"
}
```

### Camera Response

```json
{
  "id": 1,
  "name": "Main Entrance Camera",
  "code": "CAM-001",
  "zoneId": 1,
  "zoneCode": "ZONE-A",
  "location": "Building A, Floor 1",
  "streamUrl": "rtsp://camera.example.com/stream1",
  "active": true,
  "createdAt": "2024-01-15T08:00:00Z",
  "updatedAt": "2024-01-15T09:00:00Z"
}
```

### Paginated Response

```json
{
  "content": [...],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 20,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "size": 20,
  "number": 0,
  "first": true,
  "numberOfElements": 20,
  "empty": false
}
```

## Error Responses

### Error Response Schema

```json
{
  "success": false,
  "message": "Error description",
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/v1/video/upload"
}
```

### HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created successfully |
| 204 | No content (successful deletion) |
| 400 | Bad request - Invalid parameters or validation error |
| 401 | Unauthorized - Authentication required or token invalid |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not found - Resource does not exist |
| 500 | Internal server error |

## Validation Rules

### Stream Request
- `url`: Required, must be a valid URI
- `cameraId`: Required
- `zoneId`: Required
- `branchId`: Required
- `tenantId`: Required
- `username`: Max 50 characters
- `modelVersion`: Max 50 characters
- `samplingIntervalSeconds`: Between 1 and 3600

### Camera Request
- `name`: Required, max 100 characters
- `code`: Max 50 characters
- `zoneId`: Required
- `location`: Max 200 characters
- `streamUrl`: Must be valid URI format

### Frame Analysis
- `image`: Required, valid image file (JPEG, PNG, etc.)
- `confidenceThreshold`: Between 0.1 and 1.0
- `zoneConfidenceThreshold`: Between 0.1 and 1.0

## Multi-Tenancy

The API supports multi-tenant architecture. The tenant and branch context is automatically extracted from the JWT token:

- Tenant ID and Branch ID are automatically applied to all queries
- Users can only access resources within their tenant and branch
- No need to specify tenant/branch in most API calls

## Pagination

All list endpoints support pagination with the following query parameters:

- `page`: Page number (0-indexed), default: 0
- `size`: Number of items per page, default: 20, max: 100
- `sort`: Sort criteria in format `property,direction`
  - Example: `sort=createdAt,desc`
  - Example: `sort=name,asc`

## Rate Limiting

- API calls are rate-limited to prevent abuse
- Default limit: 100 requests per minute per user
- Exceed limits will result in HTTP 429 (Too Many Requests)

## Swagger UI

Interactive API documentation is available at:

```
http://localhost:8081/api/swagger-ui/index.html
```

## Support

For API support and questions:
- Email: support@pluta.com
- Documentation: https://docs.pluta.com/api

## Changelog

### Version 1.0.0 (2024-01-15)
- Initial release
- Video upload and processing
- Stream management
- Camera management
- Frame analysis
- Multi-tenant support