# Thermal Inspection API Reference

Base URL: `http://localhost:8080`

## Common Rules

- All `/api/v1/**` endpoints require a JWT unless noted otherwise.
- Send `Authorization: Bearer <JWT>` and `Content-Type: application/json` for JSON requests.
- Most successful responses are wrapped in `ApiResponse`:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {},
  "timestamp": "2026-03-31T12:45:11.456"
}
```

- Create endpoints usually return HTTP `201 Created`.
- Delete endpoints return HTTP `200 OK` with `data: null`.
- `GET /actuator/health` and `GET /actuator/info` are public.
- The `legacy` profile enables the maintenance record and annotation log APIs.

## 1. Transformer APIs

All transformer APIs use the `TransformerRequest` and `TransformerResponse` shapes.

| Method | Path | Request | Success Response |
| --- | --- | --- | --- |
| `GET` | `/api/v1/transformers` | none | `ApiResponse<List<TransformerResponse>>` |
| `GET` | `/api/v1/transformers/{id}` | path `id` | `ApiResponse<TransformerResponse>` |
| `POST` | `/api/v1/transformers` | `TransformerRequest` | `ApiResponse<TransformerResponse>` |
| `PUT` | `/api/v1/transformers/{id}` | path `id`, `TransformerRequest` | `ApiResponse<TransformerResponse>` |
| `DELETE` | `/api/v1/transformers/{id}` | path `id` | `ApiResponse<Void>` |

### TransformerRequest

`number` is required. `baselineImage` should be the S3 object key returned by the upload URL API, not the signed URL.

```json
{
  "number": "TR-0001",
  "pole": "P-17",
  "region": "North",
  "type": "Distribution",
  "baselineImage": "transformers/base/tr-0001.jpg",
  "baselineUploadDate": "2026-03-31",
  "weather": "Sunny",
  "location": "Gampaha"
}
```

### TransformerResponse

The backend maps the entity `createdAt` value into `baselineUploadDate`.

```json
{
  "id": 42,
  "number": "TR-0001",
  "pole": "P-17",
  "region": "North",
  "type": "Distribution",
  "baselineImage": "transformers/base/tr-0001.jpg",
  "baselineUploadDate": "2026-03-31T12:45:10.123",
  "weather": "Sunny",
  "location": "Gampaha",
  "inspectionCount": 0
}
```

### Create Transformer Example

`POST /api/v1/transformers`

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "number": "TR-0001",
  "pole": "P-17",
  "region": "North",
  "type": "Distribution",
  "baselineImage": "transformers/base/tr-0001.jpg",
  "weather": "Sunny",
  "location": "Gampaha"
}
```

Response:

```json
{
  "success": true,
  "message": "Transformer created successfully",
  "data": {
    "id": 42,
    "number": "TR-0001",
    "pole": "P-17",
    "region": "North",
    "type": "Distribution",
    "baselineImage": "transformers/base/tr-0001.jpg",
    "baselineUploadDate": "2026-03-31T12:45:10.123",
    "weather": "Sunny",
    "location": "Gampaha",
    "inspectionCount": 0
  },
  "timestamp": "2026-03-31T12:45:11.456"
}
```

### List Transformers Example

`GET /api/v1/transformers`

```json
{
  "success": true,
  "message": "Transformers retrieved successfully",
  "data": [
    {
      "id": 42,
      "number": "TR-0001",
      "pole": "P-17",
      "region": "North",
      "type": "Distribution",
      "baselineImage": "transformers/base/tr-0001.jpg",
      "baselineUploadDate": "2026-03-31T12:45:10.123",
      "weather": "Sunny",
      "location": "Gampaha",
      "inspectionCount": 0
    }
  ],
  "timestamp": "2026-03-31T12:45:11.456"
}
```

## 2. Inspection APIs

All inspection APIs use the `InspectionRequest` and `InspectionResponse` shapes.

| Method | Path | Request | Success Response |
| --- | --- | --- | --- |
| `GET` | `/api/v1/inspections/{id}` | path `id` | `ApiResponse<InspectionResponse>` |
| `GET` | `/api/v1/transformers/{transformerId}/inspections` | path `transformerId` | `ApiResponse<List<InspectionResponse>>` |
| `POST` | `/api/v1/transformers/{transformerId}/inspections` | path `transformerId`, `InspectionRequest` | `ApiResponse<InspectionResponse>` |
| `PUT` | `/api/v1/inspections/{id}` | path `id`, `InspectionRequest` | `ApiResponse<InspectionResponse>` |
| `DELETE` | `/api/v1/inspections/{id}` | path `id` | `ApiResponse<Void>` |

### InspectionRequest

`inspector` is required. On create, the path parameter `transformerId` is the real transformer link; the `transformerId` field in the body is optional. The backend accepts `date` or `inspectedDate` in `yyyy-MM-dd` format.

```json
{
  "date": "2026-03-31",
  "inspectedDate": "2026-03-31",
  "inspector": "Nimal Perera",
  "notes": "Routine inspection completed.",
  "status": "COMPLETED",
  "maintenanceImage": "inspections/maintenance/inspect-001.jpg",
  "maintenanceUploadDate": "2026-03-31",
  "maintenanceWeather": "Sunny",
  "annotatedImage": "inspections/annotated/inspect-001.jpg",
  "anomalies": "[]",
  "progressStatus": "DONE"
}
```

### InspectionResponse

The service returns the inspection date as both `date` and `inspectedDate`, and it maps the stored inspection image key to both `maintenanceImage` and `annotatedImage`.

```json
{
  "id": 101,
  "transformerId": 42,
  "transformerNumber": "TR-0001",
  "date": "2026-03-31",
  "inspectedDate": "2026-03-31",
  "inspector": "Nimal Perera",
  "notes": "Routine inspection completed.",
  "status": "COMPLETED",
  "maintenanceImage": "inspections/maintenance/inspect-001.jpg",
  "annotatedImage": "inspections/maintenance/inspect-001.jpg"
}
```

### Create Inspection Example

`POST /api/v1/transformers/42/inspections`

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "date": "2026-03-31",
  "inspector": "Nimal Perera",
  "notes": "Routine inspection completed.",
  "status": "COMPLETED",
  "maintenanceImage": "inspections/maintenance/inspect-001.jpg",
  "progressStatus": "DONE"
}
```

Response:

```json
{
  "success": true,
  "message": "Inspection created successfully",
  "data": {
    "id": 101,
    "transformerId": 42,
    "transformerNumber": "TR-0001",
    "date": "2026-03-31",
    "inspectedDate": "2026-03-31",
    "inspector": "Nimal Perera",
    "notes": "Routine inspection completed.",
    "status": "COMPLETED",
    "maintenanceImage": "inspections/maintenance/inspect-001.jpg",
    "annotatedImage": "inspections/maintenance/inspect-001.jpg"
  },
  "timestamp": "2026-03-31T12:50:00.000"
}
```

## 3. Anomaly / Annotation APIs

These endpoints use the `AnnotationRequest` and `AnnotationResponse` shapes, even though the route names still use `anomalies`.

| Method | Path | Request | Success Response |
| --- | --- | --- | --- |
| `GET` | `/api/v1/inspections/{inspectionId}/anomalies` | path `inspectionId` | `ApiResponse<List<AnnotationResponse>>` |
| `POST` | `/api/v1/inspections/{inspectionId}/anomalies` | path `inspectionId`, `AnnotationRequest` | `ApiResponse<AnnotationResponse>` |
| `PUT` | `/api/v1/anomalies/{id}` | path `id`, `AnnotationRequest` | `ApiResponse<AnnotationResponse>` |
| `DELETE` | `/api/v1/anomalies/{id}` | path `id` | `ApiResponse<Void>` |

### AnnotationRequest

`x`, `y`, `w`, and `h` are required. `classification` is preferred when present; otherwise the backend falls back to `severity`.

```json
{
  "annotationId": "A-001",
  "x": 128.5,
  "y": 92.0,
  "w": 44.0,
  "h": 38.5,
  "confidence": 0.94,
  "severity": "HIGH",
  "classification": "CRACK",
  "comment": "Insulator crack observed.",
  "source": "ai",
  "deleted": false,
  "userId": "12"
}
```

### AnnotationResponse

The backend derives `annotationId` from the database id when needed, and it always returns `deleted: false` in the current mapping. AI-generated annotations (produced by the `/analyze` endpoint) populate the extra detection fields; manually created annotations leave those fields `null`.

```json
{
  "id": 55,
  "inspectionId": 101,
  "annotationId": "55",
  "x": 128.5,
  "y": 92.0,
  "w": 44.0,
  "h": 38.5,
  "confidence": 0.82,
  "severity": "Potentially Faulty",
  "severityScore": 54.3,
  "classification": "PointOverload",
  "area": 892,
  "centroid": { "x": 161.4, "y": 109.2 },
  "meanDeltaE": 11.3,
  "peakDeltaE": 17.8,
  "meanHsv": { "h": 14.2, "s": 187.0, "v": 231.0 },
  "elongation": 1.4,
  "comment": null,
  "source": "ai",
  "deleted": false,
  "userId": null,
  "createdAt": "2026-03-31T12:55:00.000",
  "updatedAt": "2026-03-31T12:55:00.000"
}
```

### Create Annotation Example

`POST /api/v1/inspections/101/anomalies`

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "x": 128.5,
  "y": 92.0,
  "w": 44.0,
  "h": 38.5,
  "confidence": 0.94,
  "severity": "HIGH",
  "classification": "CRACK",
  "comment": "Insulator crack observed.",
  "source": "ai",
  "userId": "12"
}
```

Response:

```json
{
  "success": true,
  "message": "Anomaly created successfully",
  "data": {
    "id": 55,
    "inspectionId": 101,
    "annotationId": "55",
    "x": 128.5,
    "y": 92.0,
    "w": 44.0,
    "h": 38.5,
    "confidence": 0.94,
    "severity": "HIGH",
    "severityScore": null,
    "classification": "CRACK",
    "area": null,
    "centroid": null,
    "meanDeltaE": null,
    "peakDeltaE": null,
    "meanHsv": null,
    "elongation": null,
    "comment": "Insulator crack observed.",
    "source": "ai",
    "deleted": false,
    "userId": "12",
    "createdAt": "2026-03-31T12:55:00.000",
    "updatedAt": "2026-03-31T12:55:00.000"
  },
  "timestamp": "2026-03-31T12:55:01.000"
}
```

## 4. Image APIs

| Method | Path | Request | Success Response |
| --- | --- | --- | --- |
| `GET` | `/api/v1/images/generate-upload-url` | query `folder`, query `extension` | `ApiResponse<PresignedUploadResponse>` |
| `GET` | `/api/v1/images/generate-download-url` | query `key` | `ApiResponse<String>` |

### PresignedUploadResponse

Use `uploadUrl` for the direct `PUT` to S3. Store `objectKey` in the backend as the image reference.

```json
{
  "uploadUrl": "https://s3.amazonaws.com/...signed-url...",
  "objectKey": "transformers/base/abc123.jpg"
}
```

### Generate Upload URL Example

`GET /api/v1/images/generate-upload-url?folder=transformers/base&extension=jpg`

```json
{
  "success": true,
  "message": "Pre-signed upload URL generated",
  "data": {
    "uploadUrl": "https://s3.amazonaws.com/...signed-url...",
    "objectKey": "transformers/base/abc123.jpg"
  },
  "timestamp": "2026-03-31T12:45:00.000"
}
```

### Generate Download URL Example

`GET /api/v1/images/generate-download-url?key=transformers/base/abc123.jpg`

```json
{
  "success": true,
  "message": "Pre-signed download URL generated",
  "data": "https://s3.amazonaws.com/...signed-url...",
  "timestamp": "2026-03-31T12:46:00.000"
}
```

## 5. Anomaly Detection API

| Method | Path | Request | Success Response |
| --- | --- | --- | --- |
| `POST` | `/api/v1/inspections/{id}/analyze` | optional `InspectionAnalysisRequest` | `ApiResponse<AnomalyDetectionResponse>` |

### InspectionAnalysisRequest

The body is optional. If present, it only contains `sliderPercent`.

```json
{
  "sliderPercent": 15.0
}
```

### AnomalyDetectionResponse

The full response from the detection microservice is forwarded by this backend and wrapped in `ApiResponse`. The `anomalies` array is also persisted to the database — re-running `/analyze` replaces all previous AI-detected anomalies for that inspection.

```json
{
  "requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "timestamp": "2026-04-05T10:23:45.123456",
  "imageLevelLabel": "Potentially Faulty",
  "anomalyCount": 2,
  "anomalies": [
    {
      "id": "anomaly_1",
      "bbox": {
        "x": 142,
        "y": 87,
        "width": 38,
        "height": 44
      },
      "confidence": 0.82,
      "severity": "Potentially Faulty",
      "severityScore": 54.3,
      "classification": "PointOverload",
      "area": 892,
      "centroid": { "x": 161.4, "y": 109.2 },
      "meanDeltaE": 11.3,
      "peakDeltaE": 17.8,
      "meanHsv": { "h": 14.2, "s": 187.0, "v": 231.0 },
      "elongation": 1.4
    }
  ],
  "metrics": {
    "meanSsim": 0.874,
    "warpModel": "homography",
    "warpSuccess": true,
    "warpScore": 0.021,
    "thresholdPotential": 8.0,
    "thresholdFault": 12.0,
    "basePotential": 8.0,
    "baseFault": 12.0,
    "sliderPercent": 15.0,
    "scaleApplied": 1.15,
    "thresholdSource": "slider_scaled",
    "ratio": 1.5
  }
}
```

### Analyze Inspection Example

`POST /api/v1/inspections/101/analyze`

```http
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "sliderPercent": 15.0
}
```

Response:

```json
{
  "success": true,
  "message": "Inspection analysis completed",
  "data": {
    "requestId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "timestamp": "2026-04-05T10:23:45.123456",
    "imageLevelLabel": "Potentially Faulty",
    "anomalyCount": 2,
    "anomalies": [
      {
        "id": "anomaly_1",
        "bbox": {
          "x": 142,
          "y": 87,
          "width": 38,
          "height": 44
        },
        "confidence": 0.82,
        "severity": "Potentially Faulty",
        "severityScore": 54.3,
        "classification": "PointOverload",
        "area": 892,
        "centroid": { "x": 161.4, "y": 109.2 },
        "meanDeltaE": 11.3,
        "peakDeltaE": 17.8,
        "meanHsv": { "h": 14.2, "s": 187.0, "v": 231.0 },
        "elongation": 1.4
      }
    ],
    "metrics": {
      "meanSsim": 0.874,
      "warpModel": "homography",
      "warpSuccess": true,
      "warpScore": 0.021,
      "thresholdPotential": 8.0,
      "thresholdFault": 12.0,
      "basePotential": 8.0,
      "baseFault": 12.0,
      "sliderPercent": 15.0,
      "scaleApplied": 1.15,
      "thresholdSource": "slider_scaled",
      "ratio": 1.5
    }
  },
  "timestamp": "2026-04-05T10:23:46.000"
}
```

## 6. Legacy Profile APIs

These controllers are only active when the app runs with the `legacy` Spring profile.

### Annotation Logs

| Method | Path | Request | Success Response |
| --- | --- | --- | --- |
| `GET` | `/api/v1/annotation-logs` | none | `ApiResponse<List<AnnotationLogResponse>>` |
| `GET` | `/api/v1/annotation-logs/inspection/{inspectionId}` | path `inspectionId` | `ApiResponse<List<AnnotationLogResponse>>` |
| `GET` | `/api/v1/annotation-logs/export/json` | none | raw JSON string download |
| `GET` | `/api/v1/annotation-logs/export/csv` | none | raw CSV string download |

#### AnnotationLogResponse

```json
{
  "id": 9,
  "inspectionId": 101,
  "transformerId": 42,
  "transformerNumber": "TR-0001",
  "imageId": "img-001",
  "actionType": "CREATE",
  "annotationData": "{\"x\":128,\"y\":92}",
  "aiPrediction": "CRACK",
  "userAnnotation": "CRACK",
  "userId": "12",
  "timestamp": "2026-03-31T12:55:00.000",
  "notes": "Initial annotation saved"
}
```

#### Example

`GET /api/v1/annotation-logs`

```json
{
  "success": true,
  "message": "Annotation logs retrieved successfully",
  "data": [
    {
      "id": 9,
      "inspectionId": 101,
      "transformerId": 42,
      "transformerNumber": "TR-0001",
      "imageId": "img-001",
      "actionType": "CREATE",
      "annotationData": "{\"x\":128,\"y\":92}",
      "aiPrediction": "CRACK",
      "userAnnotation": "CRACK",
      "userId": "12",
      "timestamp": "2026-03-31T12:55:00.000",
      "notes": "Initial annotation saved"
    }
  ],
  "timestamp": "2026-03-31T13:05:00.000"
}
```

### Maintenance Records

| Method | Path | Request | Success Response |
| --- | --- | --- | --- |
| `GET` | `/api/v1/records` | optional query `transformer_id` | `ApiResponse<List<MaintenanceRecordResponse>>` |
| `GET` | `/api/v1/records/{id}` | path `id` | `ApiResponse<MaintenanceRecordResponse>` |
| `GET` | `/api/v1/records/transformer/{transformerId}` | path `transformerId` | `ApiResponse<List<MaintenanceRecordResponse>>` |
| `POST` | `/api/v1/records` | `MaintenanceRecordRequest` | `ApiResponse<MaintenanceRecordResponse>` |
| `PUT` | `/api/v1/records/{id}` | path `id`, `MaintenanceRecordRequest` | `ApiResponse<MaintenanceRecordResponse>` |
| `DELETE` | `/api/v1/records/{id}` | path `id` | `ApiResponse<Void>` |
| `GET` | `/api/v1/records/export/pdf/{id}` | path `id` | PDF file download |

#### MaintenanceRecordRequest

`transformerId` is required. `inspectionId` is optional.

```json
{
  "transformerId": 42,
  "inspectionId": 101,
  "engineerName": "Nimal Perera",
  "status": "COMPLETED",
  "readings": "Voltage stable. Temperature normal.",
  "recommendedAction": "Continue monitoring",
  "notes": "No major issues found.",
  "annotatedImage": "records/annotated/record-001.jpg",
  "anomalies": "[]",
  "location": "Gampaha"
}
```

#### MaintenanceRecordResponse

```json
{
  "id": 77,
  "transformerId": 42,
  "transformerNumber": "TR-0001",
  "inspectionId": 101,
  "recordTimestamp": "2026-03-31T13:10:00",
  "engineerName": "Nimal Perera",
  "status": "COMPLETED",
  "readings": "Voltage stable. Temperature normal.",
  "recommendedAction": "Continue monitoring",
  "notes": "No major issues found.",
  "annotatedImage": "records/annotated/record-001.jpg",
  "anomalies": "[]",
  "location": "Gampaha",
  "createdAt": "2026-03-31T13:10:00",
  "updatedAt": "2026-03-31T13:10:00"
}
```

#### Example

`POST /api/v1/records`

```json
{
  "transformerId": 42,
  "inspectionId": 101,
  "engineerName": "Nimal Perera",
  "status": "COMPLETED",
  "readings": "Voltage stable. Temperature normal.",
  "recommendedAction": "Continue monitoring",
  "notes": "No major issues found.",
  "annotatedImage": "records/annotated/record-001.jpg",
  "anomalies": "[]",
  "location": "Gampaha"
}
```

```json
{
  "success": true,
  "message": "Record created successfully",
  "data": {
    "id": 77,
    "transformerId": 42,
    "transformerNumber": "TR-0001",
    "inspectionId": 101,
    "recordTimestamp": "2026-03-31T13:10:00",
    "engineerName": "Nimal Perera",
    "status": "COMPLETED",
    "readings": "Voltage stable. Temperature normal.",
    "recommendedAction": "Continue monitoring",
    "notes": "No major issues found.",
    "annotatedImage": "records/annotated/record-001.jpg",
    "anomalies": "[]",
    "location": "Gampaha",
    "createdAt": "2026-03-31T13:10:00",
    "updatedAt": "2026-03-31T13:10:00"
  },
  "timestamp": "2026-03-31T13:10:01.000"
}
```

### Public Actuator Endpoints

| Method | Path | Notes |
| --- | --- | --- |
| `GET` | `/actuator/health` | public, returns `{ "status": "UP" }` when healthy |
| `GET` | `/actuator/info` | public, returns app metadata |

## Frontend Flow for Image Uploads

1. Call `GET /api/v1/images/generate-upload-url`.
2. Use `data.uploadUrl` to `PUT` the file directly to S3.
3. Save `data.objectKey` into the transformer or inspection payload.
4. Call the relevant create/update API.

## Important Notes

- Do not send the pre-signed `uploadUrl` to the backend as an image reference.
- Use the `objectKey` instead.
- For inspection creation, the backend uses the path transformer id and only requires `inspector` plus a date field in `yyyy-MM-dd` format.
- For anomaly creation, the required coordinates are `x`, `y`, `w`, and `h`.
