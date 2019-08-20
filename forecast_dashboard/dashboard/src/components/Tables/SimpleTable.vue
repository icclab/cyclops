<template>
  <div>
    <md-table v-model="users" :table-header-color="tableHeaderColor">
      <md-table-row slot="md-table-row" slot-scope="{ item }">
        <md-table-cell md-label="Name">{{ item.name }}</md-table-cell>
        <md-table-cell md-label="Country">{{ item.country }}</md-table-cell>
        <md-table-cell md-label="City">{{ item.city }}</md-table-cell>
        <md-table-cell md-label="Salary">{{ item.salary }}</md-table-cell>
      </md-table-row>
    </md-table>
    <div>
      {{info}}
    </div>
    <div>
      <md-button id="btn" v-on:click="getJson">button</md-button>
    </div>
  </div>

</template>

<script>
export default {
  name: "simple-table",
  props: {
    tableHeaderColor: {
      type: String,
      default: ""
    }
  },
  data() {
    return {
      info: null,
      data: {
    "hello": "hello"
      },
      selected: [],
      users: [
        {
          name: "Dakota Rice",
          salary: "$36,738",
          country: "Niger",
          city: "Oud-Turnhout"
        }
      ]
    };
  },
  methods:{
    getJson: function(){

      this.$axios
        .post('http://127.0.0.1:5000/test',this.data)
        .then(response => (this.info = response))
    }
  }

};
</script>
