<script setup lang="ts">
import { onMounted } from 'vue';

import { currentTheme, initTheme, switchTheme } from '@/composables/theme.js';

onMounted(() => {
    initTheme();
});
</script>

<template>
    <header class="absolute inset-x-0 top-0">
        <div class="container mx-auto flex justify-end p-4">
            <button class="overflow-hidden p-2" @click="switchTheme()">
                <transition
                    enter-active-class="transition duration-200 ease-out"
                    leave-active-class="transition duration-200 ease-in"
                    :enter-from-class="
                        currentTheme === 'dark'
                            ? 'transform -translate-y-full scale-50 opacity-0'
                            : 'transform translate-y-full scale-50 opacity-0'
                    "
                    enter-to-class="transform translate-y-0"
                    leave-from-class="transform translate-y-0"
                    :leave-to-class="
                        currentTheme === 'dark'
                            ? 'transform translate-y-full scale-50 opacity-0'
                            : 'transform -translate-y-full scale-50 opacity-0'
                    "
                    mode="out-in"
                >
                    <svg
                        v-if="currentTheme === 'dark'"
                        xmlns="http://www.w3.org/2000/svg"
                        class="size-8"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                    >
                        <path
                            d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"
                        />
                    </svg>

                    <svg
                        v-else
                        xmlns="http://www.w3.org/2000/svg"
                        class="size-8"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                    >
                        <path
                            d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"
                        />
                    </svg>
                </transition>
            </button>
        </div>
    </header>

    <main class="mx-auto h-screen max-w-4xl justify-center px-4 py-16">
        <h1 class="pb-20 text-center text-6xl text-gray-700 transition-colors dark:text-gray-100">
            LyricCast privacy policy
        </h1>
        <section>
            <p>
                Tomasz Kiljańczyk built the LyricCast app as an Ad Supported app. This SERVICE is
                provided by Tomasz Kiljańczyk at no cost and is intended for use as is.
            </p>
            <p>
                This page is used to inform visitors regarding my policies with the collection, use,
                and disclosure of Personal Information if anyone decided to use my Service.
            </p>
            <p>
                If you choose to use my Service, then you agree to the collection and use of
                information in relation to this policy. The Personal Information that I collect is
                used for providing and improving the Service. I will not use or share your
                information with anyone except as described in this Privacy Policy.
            </p>
            <p>
                The terms used in this Privacy Policy have the same meanings as in our Terms and
                Conditions, which are accessible at LyricCast unless otherwise defined in this
                Privacy Policy.
            </p>
        </section>

        <section>
            <h2>Information Collection and Use</h2>
            <p>
                For a better experience, while using our Service, I may require you to provide us
                with certain personally identifiable information. The information that I request
                will be retained on your device and is not collected by me in any way.
            </p>
            <div>
                <p>
                    The app does use third-party services that may collect information used to
                    identify you.
                </p>
                <p>Link to the privacy policy of third-party service providers used by the app</p>
                <ul>
                    <li>
                        <a
                            href="https://www.google.com/policies/privacy/"
                            target="_blank"
                            rel="noopener noreferrer"
                            >Google Play Services</a
                        >
                    </li>
                    <li>
                        <a
                            href="https://support.google.com/admob/answer/6128543?hl=en"
                            target="_blank"
                            rel="noopener noreferrer"
                            >AdMob</a
                        >
                    </li>
                </ul>
            </div>
        </section>

        <section>
            <h2>Log Data</h2>
            <p>
                I want to inform you that whenever you use my Service, in a case of an error in the
                app I collect data and information (through third-party products) on your phone
                called Log Data. This Log Data may include information such as your device Internet
                Protocol (“IP”) address, device name, operating system version, the configuration of
                the app when utilizing my Service, the time and date of your use of the Service, and
                other statistics.
            </p>
        </section>

        <section>
            <h2>Service Providers</h2>
            <p>I may employ third-party companies and individuals due to the following reasons:</p>
            <ul>
                <li>To facilitate our Service;</li>
                <li>To provide the Service on our behalf;</li>
                <li>To perform Service-related services; or</li>
                <li>To assist us in analyzing how our Service is used.</li>
            </ul>
            <p>
                I want to inform users of this Service that these third parties have access to their
                Personal Information. The reason is to perform the tasks assigned to them on our
                behalf. However, they are obligated not to disclose or use the information for any
                other purpose.
            </p>
        </section>

        <section>
            <h2>Security</h2>
            <p>
                I value your trust in providing us your Personal Information, thus we are striving
                to use commercially acceptable means of protecting it. But remember that no method
                of transmission over the internet, or method of electronic storage is 100% secure
                and reliable, and I cannot guarantee its absolute security.
            </p>
        </section>

        <section>
            <h2>Children's Privacy</h2>
            <div>
                <p>
                    These Services do not address anyone under the age of 13. I do not knowingly
                    collect personally identifiable information from children under 13 years of age.
                    In the case I discover that a child under 13 has provided me with personal
                    information, I immediately delete this from our servers. If you are a parent or
                    guardian and you are aware that your child has provided us with personal
                    information, please contact me so that I will be able to do the necessary
                    actions.
                </p>
            </div>
        </section>

        <section>
            <h2>Changes to This Privacy Policy</h2>
            <p>
                I may update our Privacy Policy from time to time. Thus, you are advised to review
                this page periodically for any changes. I will notify you of any changes by posting
                the new Privacy Policy on this page.
            </p>
            <p>This policy is effective as of 2024-12-01</p>

            <h3>Contact Us</h3>
            <p>
                If you have any questions or suggestions about my Privacy Policy, do not hesitate to
                contact me at
                <a href="mailto:thomas.kiljanczyk.dev@gmail.com"
                    >thomas.kiljanczyk.dev@gmail.com.</a
                >
            </p>

            <p>
                This privacy policy page was created at
                <a
                    href="https://privacypolicytemplate.net"
                    target="_blank"
                    rel="noopener noreferrer"
                    >privacypolicytemplate.net </a
                >and modified/generated by
                <a
                    href="https://app-privacy-policy-generator.nisrulz.com/"
                    target="_blank"
                    rel="noopener noreferrer"
                    >App Privacy Policy Generator</a
                >
            </p>
        </section>
    </main>
</template>

<style>
body {
    @apply dark:bg-gray-900 dark:text-gray-50;
}

section {
    @apply pb-7;
}

h2 {
    @apply pb-5 text-3xl font-bold;
}

h3 {
    @apply pb-5 pt-5 text-xl font-bold;
}

p {
    @apply pb-2;
}

a {
    @apply underline italic;
}

ul {
    @apply list-disc list-inside pb-2;
}
</style>
