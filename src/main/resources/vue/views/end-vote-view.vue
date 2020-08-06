<template id="end-vote-view">
    <div class="text-body-1 container">
        {{resultText}}
    </div>
</template>
<script>
    Vue.component("end-vote-view", {
        template: "#end-vote-view",
        data: () => ({
            resultText: [],
        }),
        created() {
            const precinct = this.$javalin.pathParams["precinct"];
            let urlCode = this.$javalin.queryParams["urlCode"];
            if (urlCode===undefined) {
                urlCode="empty";
            }
            fetch(`/api/end_vote/${precinct}?urlCode=${urlCode}`)
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