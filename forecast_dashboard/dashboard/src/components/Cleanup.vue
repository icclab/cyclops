<template>
  <div class = "content">
    <div class = "md-layout">

    <div
      class="md-layout-item md-medium-size-100 md-xsmall-size-100 md-size-50"
      >
      <md-card>
        <md-card-header data-background-color="red">
          <h4 class="title">Record Cleanup</h4>
          <p class="category">Target model to clean-up</p>
        </md-card-header>
        <div class="md-layout-item md-small-size-100 md-size-50">
          <md-field>
            <label>Target model</label>
            <md-input v-model="target" type="text"></md-input>
          </md-field>

        </div>

        <div class="md-layout-item md-small-size-100 md-size-50">

        <md-button class="md-danger" id="post" v-on:click="getJson">Execute</md-button>
        <md-button id="clear" v-on:click="clear">Clear</md-button>

    </div>

    </md-card>
    </div>

    <div
      class="md-layout-item md-medium-size-100 md-xsmall-size-100 md-size-50"
      >
      <md-card v-if="visible">
        <md-card-header data-background-color="orange">
          <h4 class="title">Cleanup Output</h4>
          <p class="category">Records Deleted by Type</p>
        </md-card-header>
      <md-table v-model="records" :table-header-color="tableHeaderColor">
        <md-table-row slot="md-table-row" slot-scope="{ item }">
          <md-table-cell md-label="Type">{{ item.type }}</md-table-cell>
          <md-table-cell md-label="Count">{{ item.count }}</md-table-cell>
        </md-table-row>
      </md-table>


      </md-card>
    </div>

</div>
</div>
</template>

<script>
export default {
  name: "cleanup",
  props: {
    tableHeaderColor: {
      type: String,
      default: ""
    }
  },
  data() {
    return {
      target: "",
      rules: false,
      info: null,
      visible: false,
      data: {
        "target": "",
        "rules": false
      },
      selected: [],
      records: [
        {
        type:"Bill",
        count: 0
        },
        {
        type:"Usage",
        count: 0
        },
        {
        type:"UDR",
        count: 0
        },
        {
        type:"CDR",
        count: 0
        }
      ]
    };
  },
  methods:{
    getJson: function(){
      if(this.target != ""){
        this.records = []
        this.data.target = this.target
        this.data.rules = this.rules
        this.$axios
          .post('http://127.0.0.1:5000/cleanup',this.data)
          .then(response => {
            this.info = response.data
            this.records.push({type:"Bill",count:this.info.bills})
            this.records.push({type:"Usage",count:this.info.usages})
            this.records.push({type:"UDR",count:this.info.udrs})
            this.records.push({type:"CDR",count:this.info.cdrs})
            this.visible = true
            this.$emit('update')
          })
      }
      //this.records.push({type:"Billing Rules",count:this.info.Bill_rules}),
      //this.records.push({type:"Charge Rules",count:this.info.cdr_rules})))
    },
    clear: function(){
      this.target = ""
      this.rules = false
      this.records = []
      this.visible = false
    }
  }

};
</script>
