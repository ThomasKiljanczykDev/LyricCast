# LyricCast JavaScript

This is the JavaScript part of the _LyricCast_ app.

# Architecture

This section outlines the architecture of the _LyricCast_ JavaScript part.

## Project structure

This project consists of modules:

- `iac` - contains the AWS CDK infrastructure as code
- `google-cast-custom-receiver-app` - contains the Google Cast custom receiver app
- `lyriccast-privacy-policy` - contains the privacy policy page
- `eslint-config` - contains the shareable ESLint configuration

## Architecture components

This section presents components of the _LyricCast_ JavaScript part.

### Custom Receiver Application Infrastructure

```mermaid
%%{init: {"flowchart": {"defaultRenderer": "elk"}} }%%
flowchart LR
    subgraph aws[AWS]
        subgraph s3Bucket[S3 Bucket]
            receiverApplication[Receiver Application Code]
        end
        cloudFront[CloudFront]
        s3Bucket -->|Host| cloudFront
    end

    googleCastDevice[Google Cast Device]
    cloudFront -->|Fetch| googleCastDevice
```
