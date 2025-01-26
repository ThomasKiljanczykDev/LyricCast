/*
 * Created by Thomas Kiljanczyk on 24/11/2024, 00:48
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 24/11/2024, 00:48
 */
import * as cdk from 'aws-cdk-lib';
import { aws_certificatemanager as cm, aws_route53 as route53 } from 'aws-cdk-lib';
import { Construct } from 'constructs';



import { DomainNameConstants } from '@/utils/constants';
import { env } from '@/utils/env';
import type { BaseStackProps } from '@/utils/props';





class CertificatesStack extends cdk.Stack {
    public readonly lyricCastReceiverCertificate: cm.Certificate;
    public readonly lyricCastPrivacyPolicyCertificate: cm.ICertificate;

    constructor(scope: Construct, id: string, props: BaseStackProps) {
        super(scope, id, {
            ...props,
            env: {
                ...props.env,
                // Certificates must be created in us-east-1
                region: 'us-east-1'
            }
        });

        const hostedZone = route53.HostedZone.fromLookup(this, 'hosted-zone', {
            domainName: props.domainNameBase
        });

        this.lyricCastReceiverCertificate = new cm.Certificate(
            this,
            'lyriccast-receiver-certificate',
            {
                domainName: DomainNameConstants.getLyricCastReceiverDomainName(
                    props.domainNameBase
                ),
                validation: cm.CertificateValidation.fromDns(hostedZone)
            }
        );

        this.lyricCastPrivacyPolicyCertificate = env.LYRICCAST_PRIVACY_POLICY_CERTIFICATE_ARN
            ? cm.Certificate.fromCertificateArn(
                  this,
                  'lyriccast-privacy-policy-certificate',
                  env.LYRICCAST_PRIVACY_POLICY_CERTIFICATE_ARN
              )
            : new cm.Certificate(this, 'lyriccast-privacy-policy-certificate', {
                  domainName: DomainNameConstants.getLyricCastPrivacyPolicyShortDomainName(
                      props.domainNameBase
                  ),
                  subjectAlternativeNames: [
                      DomainNameConstants.getLyricCastPrivacyPolicyDomainName(props.domainNameBase)
                  ],
                  validation: cm.CertificateValidation.fromDns(hostedZone)
              });
    }
}

export default CertificatesStack;
