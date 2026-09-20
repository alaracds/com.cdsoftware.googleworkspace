# com.cdsoftware.googleworkspace

- Copyright: 2026 https://www.casadelsoftware.com
- Repository: <https://github.com/alaracds/com.cdsoftware.googleworkspace>
- License: GPL 2

## Description

Google Workspace integration for iDempiere 13. The plugin exposes a Google Chat webhook through `idempiere-rest`, authenticates Google-signed OIDC tokens, maps Google Chat spaces and users to iDempiere records, and lets authorized users query requests through commands and interactive cards.

## Contributors

- 2026 Casa del Software.

## Components

- iDempiere Plugin [com.cdsoftware.googleworkspace](com.cdsoftware.googleworkspace)
- iDempiere Unit Test Fragment [com.cdsoftware.googleworkspace.test](com.cdsoftware.googleworkspace.test)

## Prerequisites

- Java 17, commands `java` and `javac`.
- iDempiere 13.0.0.
- `com.trekglobal.idempiere.rest.api` 1.0.0 or later.
- Google Chat application configured to call the public HTTPS webhook endpoint.
- Application Dictionary tables `CDS_GChatSpace` and `CDS_GChatUser` configured in iDempiere.

## Features/Documentation

### Source Structure

```text
com.cdsoftware.googleworkspace/src/com/cdsoftware/googleworkspace
├── base
│   ├── BundleInfo.java
│   ├── CustomCallout.java
│   ├── CustomForm.java
│   └── CustomProcess.java
├── chat
│   ├── GoogleChatActionService.java
│   ├── GoogleChatCommandService.java
│   ├── GoogleChatEvent.java
│   ├── GoogleChatExecutionContext.java
│   ├── GoogleChatSpaceService.java
│   ├── GoogleChatUserService.java
│   ├── card
│   │   ├── RequestCardBuilder.java
│   │   ├── RequestsCardBuilder.java
│   │   └── RequestUpdatesCardBuilder.java
│   ├── command
│   │   ├── RequestCommand.java
│   │   └── RequestDetailCommand.java
│   └── format
│       └── GoogleChatHtmlFormatter.java
├── component
│   ├── GoogleWorkspaceResourceExtension.java
│   └── ...
├── oidc
│   └── GoogleOIDCProvider.java
├── rest
│   └── GoogleChatResource.java
├── service
│   ├── GoogleChatAuthorizationService.java
│   └── RequestService.java
└── util
    └── ...
```

### REST and OIDC Integration

`GoogleWorkspaceResourceExtension` registers `GoogleChatResource` with `idempiere-rest`. The resource provides:

- `GET /api/v1/google/hello` as a connectivity check.
- `POST /api/v1/google/chat` for Google Chat events, commands, and button actions.

`GoogleOIDCProvider` is registered as an OSGi `IOIDCProvider` service with the name `Google`. It relies on `idempiere-rest` to verify the JWT signature, issuer, audience, and expiration, then validates the configured Google service-account email and its `email_verified` claim. The provider maps the request to the active iDempiere user `gchat`, the `Web Service Execution` role, organization `11`, and an iDempiere session.

The corresponding `REST_OIDCService` record must select provider `Google`, belong to the intended client, use Google's OIDC issuer and key-discovery metadata, and configure the webhook HTTPS URL as the token audience. The current provider accepts `service-396259929666@gcp-sa-gsuiteaddons.iam.gserviceaccount.com`; changing the Google Chat application requires keeping this identity mapping synchronized in the source and iDempiere configuration.

### Google Chat Request Commands

| Command | Purpose |
| --- | --- |
| `/solicitudes` | Lists the five most recent open requests for the business partner associated with the current space. |
| `/solicitudes abiertas` | Lists open requests using `R_Status.IsOpen='Y'`. |
| `/solicitudes cerradas` | Lists closed requests using `R_Status.IsClosed='Y'`. |
| `/solicitudes todas` | Lists requests without an open/closed status filter. |
| `/solicitud <DocumentNo>` | Displays one request belonging to the business partner associated with the current space. |

Request cards expose interactive actions for opening request details and loading recent `R_RequestUpdate` entries. Queries apply the active client, active-record, and iDempiere access filters.

### Authorization

`GoogleChatAuthorizationService` obtains the active roles assigned to the mapped `AD_User` and retains only roles that can read `R_Request`. Access requires an active accessible window containing the table and read access to the table itself. Commands and interactive actions execute with the user and role stored in `CDS_GChatUser`; having any role is not sufficient.

Each active Google Chat mapping must provide:

- `CDS_GChatSpace.GoogleSpaceID` and its associated `C_BPartner_ID`.
- `CDS_GChatUser.GoogleUserID`, `AD_User_ID`, and `AD_Role_ID`.

The selected user/role must be an active assignment and must have organization, window, and read access appropriate for `R_Request` and `R_RequestUpdate` data.

### OSGi Components

The bundle registers the REST resource and Google OIDC provider through Declarative Services descriptors in `OSGI-INF`. It also includes the standard model, process, callout, event, and form factories supplied by the plugin scaffold. No processes, events, callouts, or 2Pack archives are currently implemented in this repository.

## Instructions

1. Deploy `com.trekglobal.idempiere.rest.api` and this OSGi bundle in iDempiere 13.
2. Create the required `CDS_GChatSpace` and `CDS_GChatUser` Application Dictionary tables if they are not already installed; this repository does not currently include a 2Pack for them.
3. Configure the technical `gchat` user, its `Web Service Execution` role, organization access, and table/window read permissions.
4. Configure a `REST_OIDCService` record for provider `Google` and set its audience to the public HTTPS URL ending in `/api/v1/google/chat`.
5. Register that URL as the Google Chat application endpoint.
6. Map each Google Chat space to an iDempiere business partner and each Google Chat user to an authorized iDempiere user and role.

## Extra Links

- [iDempiere](https://www.idempiere.org/)
- [Google Chat API documentation](https://developers.google.com/workspace/chat)
