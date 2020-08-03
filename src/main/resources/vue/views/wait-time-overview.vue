<template id="wait-time-overview">
    <div class="container">
        <v-simple-table>
            <template v-slot:default>
                <thead>
                <tr>
                    <th>Precinct</th>
                    <th>Name</th>
                    <th>Wait Time (in Minutes)</th>
                </tr>
                </thead>
                <tbody style="text-align:left">
                <tr v-for="wt in waitTimes">
                    <td>{{wt.precinct}}</td>
                    <td>{{wt.name}}</td>
                    <td>{{wt.waitTime==-1 ? "--":wt.waitTime}}</td>
                </tr>
                </tbody>
            </template>
        </v-simple-table>
    </div>
</template>
<script>
    Vue.component("wait-time-overview", {
        template: "#wait-time-overview",
        data: () => ({
            waitTimes: [],
        }),
        created() {
            fetch("/api/wait_time_overview")
                .then(res => res.json())
                .then(res => this.waitTimes = res)
                .catch(() => alert("Error while fetching wait times"));
        }
    });
</script>
<style>
    div.container {
        position: relative;
    }
</style>