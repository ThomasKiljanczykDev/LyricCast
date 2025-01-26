import * as cdk from 'aws-cdk-lib';
import {
    aws_cloudfront as cloudFront,
    aws_cloudfront_origins as cloudFrontOrigins,
    aws_certificatemanager as cm,
    aws_route53 as route53,
    aws_s3 as s3
} from 'aws-cdk-lib';
import { Construct } from 'constructs';

import { DomainNameConstants } from '@/utils/constants';
import type { BaseStackProps } from '@/utils/props';

export interface LyricCastReceiverStackProps extends BaseStackProps {
    readonly lyricCastReceiverCertificate: cm.Certificate;
}

export default class LyricCastReceiverStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props: LyricCastReceiverStackProps) {
        super(scope, id, props);

        const hostedZone = route53.HostedZone.fromLookup(this, 'hosted-zone', {
            domainName: props.domainNameBase
        });

        const s3CorsRule: s3.CorsRule = {
            allowedMethods: [s3.HttpMethods.GET, s3.HttpMethods.HEAD],
            allowedOrigins: ['*'],
            allowedHeaders: ['*'],
            maxAge: 300
        };

        const s3Bucket = new s3.Bucket(this, 's3-bucket', {
            bucketName: `lyriccast-receiver-${props.deploymentEnvironment}`,
            blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
            accessControl: s3.BucketAccessControl.PRIVATE,
            removalPolicy: cdk.RemovalPolicy.DESTROY,
            cors: [s3CorsRule],
            enforceSSL: true
        });

        const viewerRequestFunction = new cloudFront.Function(this, 'viewer-request-function', {
            code: cloudFront.FunctionCode.fromFile({
                filePath: 'lib/cloudfront-functions/viewer-redirect.js'
            }),
            runtime: cloudFront.FunctionRuntime.JS_2_0
        });

        const viewerResponseFunction = new cloudFront.Function(this, 'viewer-response-function', {
            code: cloudFront.FunctionCode.fromFile({
                filePath: 'lib/cloudfront-functions/viewer-security-headers.js'
            }),
            runtime: cloudFront.FunctionRuntime.JS_2_0
        });

        const cloudFrontDistribution = new cloudFront.Distribution(
            this,
            'cloud-front-distribution',
            {
                comment: `lyriccast-receiver-${props.deploymentEnvironment}`,
                defaultBehavior: {
                    origin: cloudFrontOrigins.S3BucketOrigin.withOriginAccessControl(s3Bucket),
                    viewerProtocolPolicy: cloudFront.ViewerProtocolPolicy.REDIRECT_TO_HTTPS,
                    functionAssociations: [
                        {
                            function: viewerRequestFunction,
                            eventType: cloudFront.FunctionEventType.VIEWER_REQUEST
                        },
                        {
                            function: viewerResponseFunction,
                            eventType: cloudFront.FunctionEventType.VIEWER_RESPONSE
                        }
                    ]
                },
                priceClass: cloudFront.PriceClass.PRICE_CLASS_100,
                httpVersion: cloudFront.HttpVersion.HTTP2_AND_3,
                domainNames: [
                    DomainNameConstants.getLyricCastReceiverDomainName(props.domainNameBase)
                ],
                certificate: props.lyricCastReceiverCertificate
            }
        );

        new route53.CnameRecord(this, 'lyriccast-receiver-cname-record', {
            zone: hostedZone,
            recordName: DomainNameConstants.getLyricCastReceiverDomainName(props.domainNameBase),
            domainName: cloudFrontDistribution.distributionDomainName
        });
    }
}
