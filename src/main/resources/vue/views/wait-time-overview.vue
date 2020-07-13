<template id="wait-time-overview">
    <div class="container">
        <img src="/Images/header3.png" width="910" height="193" alt="banner">
        <table>
            <thead>
            <tr>
                <th>Precinct</th>
                <th>Wait Time</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="wt in waitTimes">
                <td>{{wt.precinct}}</td>
                <td>{{wt.waitTime}}</td>
            </tr>
            </tbody>
        </table>
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
    }
    ul.wait-time-overview-list {
        padding: 0;
        list-style: none;
    }
    ul.wait-time-overview-list li {
        display: block;
        padding: 16px;
        border-bottom: 1px solid #ddd;
    }
    table {
        border: 2px solid #42b983;
        border-radius: 3px;
        background-color: #fff;
        text-align: center;
    }
    th {
        background-color: #42b983;
        color: rgba(255, 255, 255, 0.66);
        cursor: pointer;
        -webkit-user-select: none;
        -moz-user-select: none;
        -ms-user-select: none;
        user-select: none;
    }

    td {
        background-color: #f9f9f9;
    }

    th,
    td {
        min-width: 120px;
        padding: 10px 20px;
    }
</style>