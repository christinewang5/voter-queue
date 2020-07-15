<template id="wait-time-overview">
    <div class="container">
        <img src="/Images/header3.png" width="910" height="193" alt="banner">
          <v-simple-table>
            <template v-slot:default>
                <thead>
                    <tr>
                        <th>Precinct</th>
                        <th>Wait Time (minutes)</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="wt in waitTimes">
                        <td>{{wt.precinct}}</td>
                        <td>{{wt.waitTime}}</td>
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
                .catch(() => alert("Error while fetching users"));
        }
    });
</script>
<style>
    div.container {
        position: relative;
        text-align: center;
    }
</style>