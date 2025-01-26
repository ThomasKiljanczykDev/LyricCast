/*
 * Created by Thomas Kiljanczyk on 24/11/2024, 00:52
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 24/11/2024, 00:52
 */

export class DomainNameConstants {
    public static getLyricCastReceiverDomainName(domain: string): string {
        return `lyriccast-receiver.${domain}`;
    }

    public static getLyricCastPrivacyPolicyDomainName(domain: string): string {
        return `lyriccast-privacy-policy.${domain}`;
    }

    public static getLyricCastPrivacyPolicyRootDomainName(): string {
        return `lyriccast-privacy-policy.thomas-kiljanczyk.dev`;
    }

    public static getLyricCastPrivacyPolicyShortDomainName(domain: string): string {
        return `privacy-policy.${domain}`;
    }
}
