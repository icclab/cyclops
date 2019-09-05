<template>
  <div class = "content">
    <div class = "md-layout">

    <div
      class="md-layout-item md-medium-size-100 md-xsmall-size-100 md-size-100"
      >
      <md-card>
        <md-card-header data-background-color="purple">
          <h4 class="title">Input</h4>
          <p class="category">Select forecast parameters</p>
        </md-card-header>
        <div class="md-layout-item md-small-size-100 md-size-100">
        <md-field>
          <label for="movie">Type</label>
          <md-select v-model="type" name="type" id="type">
            <md-option value="single">Single account</md-option>
            <md-option value="global">Global</md-option>
            <md-option value="pattern">Pattern-based global</md-option>
          </md-select>
        </md-field>
      </div>
        <div v-if="type === 'single'" class="md-layout-item md-small-size-100 md-size-100">
          <md-field>
            <label>Account</label>
            <md-select v-model="account" name="account" id="account">
              <md-option v-for="account in options" :value="account.value">{{account.text}}</md-option>
            </md-select>
          </md-field>
          </div>

        <div class="md-layout-item md-small-size-100 md-size-100">
          <md-field>
            <label>Target model</label>
            <md-input v-model="target" type="text"></md-input>
          </md-field>
          </div>
          <div class="md-layout-item md-small-size-100 md-size-100">
            <md-field>
              <label>Forecast length</label>
              <md-input v-model="size" type="number"></md-input>
            </md-field>

        </div>

        <div class="md-layout-item md-small-size-100 md-size-100">

        <md-button class="md-success" id="post" v-on:click="getJson">Execute</md-button>
        <md-button id="clear" v-on:click="clear">Clear</md-button>

    </div>

    </md-card>
    </div>



</div>
</div>
</template>

<script>

export default {

  name: "forecast",
  props: {
    tableHeaderColor: {
      type: String,
      default: ""
    }
  },
  data() {
    return {
      type: "single",
      account: null,
      accounts: null,
      options: [],
      target: "",
      size: 30,
      info: null,
      data: {},
      bills: []

    };
  },
  mounted(){
    this.$axios
      .get('http://127.0.0.1:5000/accounts')
      .then(response => {
        this.accounts = response.data;
          for (var option in this.accounts){
            if (!this.accounts[option].includes("forecast")){
              this.options.push({'value':this.accounts[option],'text':this.accounts[option]})
            }
          }
      })
  },
  methods:{
    getJson: function(){
      if(this.type === "single"){
        this.data = {
          "account": this.account,
          "target": this.target,
          "size": this.size
        }
        this.$axios
          .post('http://127.0.0.1:5000/singleforecast',this.data)
          .then(response => {
            this.info = response.data;
            for (var key in this.info){
              this.bills.push({account:key,charge:this.info[key] + "CHF"})
            }
            this.$emit('update')
          })
      }
      else if(this.type === "global"){
        this.data = {
          "target": this.target,
          "size": this.size
        }
        this.$axios
          .post('http://127.0.0.1:5000/globalforecast',this.data)
          .then(response => {
            this.info = response.data;
            for (var key in this.info){
              this.bills.push({account:key,charge:this.info[key] + "CHF"})
            }
            this.$emit('update')
          })
      }
      else if(this.type === "pattern"){
        this.data = {
          "target": this.target,
          "size": this.size
        }
        this.$axios
          .post('http://127.0.0.1:5000/patternforecast',this.data)
          .then(response => {
            this.info = response.data;
            for (var key in this.info){
              this.bills.push({account:key,charge:this.info[key] + "CHF"})
            }
            this.$emit('update')
          })
      }

    },
    clear: function(){
      this.type = "single"
      this.target = ""
      this.account = ""
      this.size = 30
      this.data = {}
      this.info = ""
    }
  }

};
</script>
