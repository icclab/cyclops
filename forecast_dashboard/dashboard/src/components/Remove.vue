<template>
  <div class = "content">
    <div class = "md-layout">

    <div
      class="md-layout-item md-medium-size-100 md-xsmall-size-100 md-size-100"
      >
      <md-card>

        <div class="md-layout-item md-small-size-100 md-size-50">
          <md-field>
            <label>Target instance</label>
            <md-select v-model="target" name = "target" id="target">
              <md-option value="cdr">Coin CDR</md-option>
              <md-option value="bill">Coin Bill</md-option>
            </md-select>
          </md-field>
          </div>
          <div v-if="target === 'cdr'" class="md-layout-item md-small-size-100 md-size-100">
            <md-field>
              <label>Rule name</label>
              <md-input v-model="rule" type="text"></md-input>
            </md-field>
          </div>
          <div v-if="target === 'bill'" class="md-layout-item md-small-size-100 md-size-100">
            <md-field>
              <label>Rule name</label>
              <md-input v-model="rule" type="text"></md-input>
            </md-field>
          </div>

        <div class="md-layout-item md-small-size-100 md-size-50">

        <md-button class="md-danger" id="post" v-on:click="getJson">Execute</md-button>
        <md-button id="clear" v-on:click="clear">Clear</md-button>

    </div>

    </md-card>
    </div>



</div>
</div>
</template>

<script>
export default {
  name: "remove",
  props: {
    tableHeaderColor: {
      type: String,
      default: ""
    }
  },
  data() {
    return {
      target: "",
      rule: "",
      info: null,
      cdrrules: [],
      billrules: [],
      data: {
        "target": "",
        "rule": ""
      },
      selected: [],
      records: []
    };
  },
  methods:{
    getJson: function(){
      this.data.target = this.target
      this.data.rule = this.rule
      this.$axios
        .post('http://127.0.0.1:5000/removerule',this.data)
        .then(response => {
          this.info = response.data;
          this.$root.$emit('list');
        })
    },
    clear: function(){
      this.target = ""
      this.rule = ""
      this.info = ""
    }
  }

};
</script>
