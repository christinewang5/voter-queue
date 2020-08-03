<template id="toolbar">
    <v-toolbar prominent>
        <v-toolbar-title>
            <div class="d-flex align-center float-sm-left">
                <div class="float-left">
                    <v-img min-height="20px" min-width="20px" max-height="60px" max-width="60px"
                           src="https://harrisrecovery.org/wp-content/uploads/2019/07/harris-county-circle.png"/>
                </div>
                <div class="toolbar-text text-lg-h2 text-sm-h5">
                    Harris County Voting Queue
                </div>
            </div>
        </v-toolbar-title>
    </v-toolbar>
</template>
<script>
    Vue.component("toolbar", {
        template: "#toolbar",
    });
</script>
<style>
    div.toolbar-text {
        max-width: 100%;
    }
</style>