# Dhay Core API
## Version: 1.0

### Servers

| URL | Description |
| --- | ----------- |
| http://localhost:8000 | API Gateway |

### Available authorizations
#### bearerAuth (HTTP, bearer)
Bearer format: JWT

---

### [PUT] /api/v1/passengers/requests/{id}
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | long |
| X-User-Id | header |  | Yes | long |

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [PassengerTripRequestDTO](#passengertriprequestdto-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

### [POST] /api/v1/passengers/requests
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| X-User-Id | header |  | Yes | long |

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [PassengerTripRequestDTO](#passengertriprequestdto-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

### [POST] /api/v1/passengers/estimate
#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [LocationRequestDTO](#locationrequestdto-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: [PriceEstimationResponse](#priceestimationresponse-schema)<br> |

### [PATCH] /api/v1/passengers/requests/{id}/cancel
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | long |
| X-User-Id | header |  | Yes | long |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

---

### [POST] /api/v1/reviews/submit
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| tripId | query |  | Yes | long |
| revieweeId | query |  | Yes | long |
| rating | query |  | Yes | integer |
| comment | query |  | No | string |
| type | query |  | Yes | string, <br>**Available values:** "PASSENGER_TO_DRIVER", "DRIVER_TO_PASSENGER" |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: string<br> |

---

### [POST] /api/v1/devices/test-push
#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [NotificationTestRequest](#notificationtestrequest-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: string<br> |

### [POST] /api/v1/devices/register
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| token | query |  | Yes | string |
| deviceType | query |  | No | string, <br>**Default:** android |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: string<br> |

### [DELETE] /api/v1/devices/unregister
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| token | query |  | Yes | string |

#### Responses

| Code | Description |
| ---- | ----------- |
| 200 | OK |

---

### [POST] /api/v1/auth/social-login
#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [SocialLoginRequest](#socialloginrequest-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: [UserResponse](#userresponse-schema)<br> |

### [POST] /api/v1/auth/register
#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [RegisterRequest](#registerrequest-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: [UserResponse](#userresponse-schema)<br> |

### [POST] /api/v1/auth/login
#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [LoginRequest](#loginrequest-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: [UserResponse](#userresponse-schema)<br> |

---

### [POST] /api/drivers/trips
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| X-User-Id | header |  | Yes | long |

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [TripCreateDTO](#tripcreatedto-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

### [POST] /api/drivers/trips/reject-passenger/{requestId}
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| requestId | path |  | Yes | long |
| X-User-Id | header |  | Yes | long |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

### [POST] /api/drivers/trips/confirm-passenger
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| X-User-Id | header |  | Yes | long |

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [ConfirmBookingRequest](#confirmbookingrequest-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

### [POST] /api/drivers/register
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| X-User-Id | header |  | Yes | long |

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [DriverRegistrationRequest](#driverregistrationrequest-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

### [PATCH] /api/drivers/trips/{tripId}/start
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| tripId | path |  | Yes | long |
| X-User-Id | header |  | Yes | long |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

### [PATCH] /api/drivers/trips/{tripId}/complete
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| tripId | path |  | Yes | long |
| X-User-Id | header |  | Yes | long |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

### [PATCH] /api/drivers/trips/{tripId}/cancel
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| tripId | path |  | Yes | long |
| X-User-Id | header |  | Yes | long |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

### [GET] /api/drivers/trips/recent
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| X-User-Id | header |  | Yes | long |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: [ [RecentTripResponseDTO](#recenttripresponsedto-schema) ]<br> |

### [GET] /api/drivers/check-registration
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| X-User-Id | header |  | Yes | long |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

---

### [PATCH] /api/v1/trips/{id}/confirm
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | long |
| X-User-Id | header |  | Yes | long |

#### Request Body

| Required | Schema |
| -------- | ------ |
|  Yes | **application/json**: [TripConfirmRouteRequest](#tripconfirmrouterequest-schema)<br> |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

### [GET] /api/v1/trips/{id}/suggested-routes
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | long |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

---

### [GET] /api/v1/users/me
#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

---

### [GET] /api/v1/system/time-check
#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

---

### [GET] /api/v1/matching/trigger/{id}
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | long |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: string<br> |

### [GET] /api/v1/matching/scan/{tripId}
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| tripId | path |  | Yes | long |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

### [GET] /api/v1/matching/optimized-results/{tripId}
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| tripId | path |  | Yes | long |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: object<br> |

---

### [GET] /api/v1/internal/trip-requests/{id}/payment-info
#### Parameters

| Name | Located in | Description | Required | Schema |
| ---- | ---------- | ----------- | -------- | ------ |
| id | path |  | Yes | long |
| X-Internal-Key | header |  | Yes | string |

#### Responses

| Code | Description | Schema |
| ---- | ----------- | ------ |
| 200 | OK | ***/***: [PaymentInfoResponse](#paymentinforesponse-schema)<br> |

---
### Schemas

#### PassengerTripRequestDTO Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| startAddress | string |  | No |
| startLat | double |  | No |
| startLng | double |  | No |
| endAddress | string |  | No |
| endLat | double |  | No |
| endLng | double |  | No |
| departureTime | dateTime |  | No |
| numberOfSeats | integer |  | No |
| note | string |  | No |
| selectedVehicleType | string, <br>**Available values:** "MOTORBIKE", "CAR_4_SEATER", "CAR_7_SEATER" | *Enum:* `"MOTORBIKE"`, `"CAR_4_SEATER"`, `"CAR_7_SEATER"` | No |
| confirmedPrice | number |  | No |

#### LocationRequestDTO Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| startLat | double |  | No |
| startLng | double |  | No |
| endLat | double |  | No |
| endLng | double |  | No |

#### PriceEstimationResponse Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| distanceKm | double |  | No |
| options | [ [VehiclePriceOption](#vehiclepriceoption-schema) ] |  | No |

#### VehiclePriceOption Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| type | string, <br>**Available values:** "MOTORBIKE", "CAR_4_SEATER", "CAR_7_SEATER" | *Enum:* `"MOTORBIKE"`, `"CAR_4_SEATER"`, `"CAR_7_SEATER"` | No |
| label | string |  | No |
| totalPrice | number |  | No |

#### NotificationTestRequest Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| targetUserId | long |  | No |
| title | string |  | No |
| body | string |  | No |
| type | string |  | No |
| data | object |  | No |

#### SocialLoginRequest Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| email | string |  | No |
| fullName | string |  | Yes |
| avatarUrl | string |  | No |
| identifier | string |  | Yes |
| provider | string, <br>**Available values:** "LOCAL", "GOOGLE", "FACEBOOK" | *Enum:* `"LOCAL"`, `"GOOGLE"`, `"FACEBOOK"` | Yes |

#### UserResponse Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | No |
| fullName | string |  | No |
| email | string |  | No |
| avatarUrl | string |  | No |
| accessToken | string |  | No |

#### RegisterRequest Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| fullName | string |  | Yes |
| email | string |  | No |
| identifier | string |  | Yes |
| password | string |  | No |
| provider | string, <br>**Available values:** "LOCAL", "GOOGLE", "FACEBOOK" | *Enum:* `"LOCAL"`, `"GOOGLE"`, `"FACEBOOK"` | No |

#### LoginRequest Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| identifier | string |  | Yes |
| password | string |  | Yes |
| provider | string, <br>**Available values:** "LOCAL", "GOOGLE", "FACEBOOK" | *Enum:* `"LOCAL"`, `"GOOGLE"`, `"FACEBOOK"` | No |

#### TripCreateDTO Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| startAddress | string |  | Yes |
| startLat | double |  | Yes |
| startLng | double |  | Yes |
| endAddress | string |  | Yes |
| endLat | double |  | Yes |
| endLng | double |  | Yes |
| departureTime | dateTime |  | Yes |
| vehicleId | long |  | Yes |
| totalSeats | integer |  | Yes |
| note | string |  | No |

#### ConfirmBookingRequest Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| tripId | long |  | Yes |
| passengerRequestId | long |  | Yes |

#### DriverRegistrationRequest Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| licenseNumber | string |  | Yes |
| vehiclePlate | string |  | Yes |
| vehicleBrand | string |  | Yes |
| capacity | integer |  | No |
| vehicleType | string, <br>**Available values:** "MOTORBIKE", "CAR_4_SEATER", "CAR_7_SEATER" | *Enum:* `"MOTORBIKE"`, `"CAR_4_SEATER"`, `"CAR_7_SEATER"` | Yes |
| vehicleModel | string |  | Yes |

#### TripConfirmRouteRequest Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| polyline | string |  | No |
| estimatedArrivalTime | dateTime |  | No |
| distanceKm | double |  | No |
| durationMinutes | double |  | No |
| routeName | string |  | No |

#### PaymentInfoResponse Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| bookingId | long |  | No |
| tripId | long |  | No |
| amount | number |  | No |
| currency | string |  | No |
| status | string |  | No |
| description | string |  | No |

#### RecentTripResponseDTO Schema

| Name | Type | Description | Required |
| ---- | ---- | ----------- | -------- |
| id | long |  | No |
| distanceKm | double |  | No |
| durationMinutes | integer |  | No |
| startAddress | string |  | No |
| endAddress | string |  | No |
