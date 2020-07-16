<template id="toolbar">
    <v-toolbar prominent>
        <v-toolbar-title>
            <div class="d-flex float-sm-left">
                <div class="float-left">
                    <v-img max-height="40px" max-width="40px"
                           src="https://harrisrecovery.org/wp-content/uploads/2019/07/harris-county-circle.png"/>
                </div>
                <div class="toolbar-text text-h6">
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