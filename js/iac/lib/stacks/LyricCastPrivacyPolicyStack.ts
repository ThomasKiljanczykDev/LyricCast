import * as cdk from 'aws-cdk-lib';
import {
    aws_cloudfront as cloudFront,
    aws_certificatemanager as cm,
    aws_route53 as route53
} from 'aws-cdk-lib';
import { Construct } from 'constructs';

import { CloudFrontToS3 } from '@aws-solutions-constructs/aws-cloudfront-s3';

import { DomainNameConstants } from '@/utils/constants';
import { DeploymentEnvironment, env } from '@/utils/env';
import type { BaseStackProps } from '@/utils/props';

export interface LyricCastPrivacyPolicyStackProps extends BaseStackProps {
    readonly certificate: cm.ICertificate;
}

export default class LyricCastPrivacyPolicyStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props: LyricCastPrivacyPolicyStackProps) {
        super(scope, id, props);

        const hostedZone = route53.HostedZone.fromLookup(this, 'hosted-zone', {
            domainName: props.domainNameBase
        });

        const domainNames = [
            DomainNameConstants.getLyricCastPrivacyPolicyDomainName(props.domainNameBase)
        ];

        if (env.DEPLOYMENT_ENVIRONMENT === DeploymentEnvironment.PRODUCTION) {
            domainNames.push(DomainNameConstants.getLyricCastPrivacyPolicyRootDomainName());
        }

        const cloudFrontS3 = new CloudFrontToS3(this, 'cloudfront-s3', {
            bucketProps: {
                bucketName: `lyriccast-privacy-policy-${props.deploymentEnvironment}`
            },
            cloudFrontDistributionProps: {
                comment: `lyriccast-privacy-policy-${props.deploymentEnvironment}`,
                priceClass: cloudFront.PriceClass.PRICE_CLASS_100,
                httpVersion: cloudFront.HttpVersion.HTTP2_AND_3,
                domainNames: domainNames,
                certificate: props.certificate,
                defaultRootObject: 'index.html'
            }
        });

        new route53.CnameRecord(this, 'cname-record', {
            zone: hostedZone,
            recordName: DomainNameConstants.getLyricCastPrivacyPolicyDomainName(
                props.domainNameBase
            ),
            domainName: cloudFrontS3.cloudFrontWebDistribution.distributionDomainName
        });
    }
}
