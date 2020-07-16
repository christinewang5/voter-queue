<template id="start-vote-view">
    <div class="text-body-1 container">
        {{resultText}}
    </div>
</template>
<script>
    Vue.component("start-vote-view", {
        template: "#start-vote-view",
        data: () => ({
            resultText: [],
        }),
        created() {
            const precinct = this.$javalin.pathParams["precinct"];
            fetch(`/api/start_vote/${precinct}`)
                    .then(res => res.text())
                    .then(res => this.resultText = res)
                    .catch(() => alert("Error while text"));
        }
    });
</script>
<style>
    div.container {
        position: relative;
        padding: 64px;
        text-align: center;
        vertical-align: center;
    }
</style>