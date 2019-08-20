<template>
  <div>
    <nav-tabs-card >
      <template slot="content">
        <span class="md-nav-tabs-title">Rules:</span>
        <md-tabs class="md-info" md-alignment="left">
          <md-tab id="tab-coin" md-label="Coin CDR" md-icon="code">
            <md-card>
              <md-card-header data-background-color="orange">
                <h4 class="title">CDR Rules</h4>
                <p class="category">Rules for generating CDRs from UDRs</p>
              </md-card-header>
            <md-table v-model="cdrrules" :table-header-color="tableHeaderColor">
              <md-table-row slot="md-table-row" slot-scope="{ item }">
                <md-table-cell md-label="Name">{{ item.name }}</md-table-cell>
                <md-table-cell md-label="Rule"><pre>{{ item.rule }}</pre></md-table-cell>
                <md-table-cell md-label="Delete">
                  <md-button class="md-just-icon md-simple md-danger" v-on:click="deleteRule('cdr',item.name)" id="delete">
                    <md-icon>close</md-icon>
                    <md-tooltip md-direction="top">Delete</md-tooltip>
                  </md-button>
                </md-table-cell>
              </md-table-row>
            </md-table>
          </md-card>
        </md-tab>
        <md-tab id="tab-bill" md-label="Coin Bill" md-icon="code">
          <md-card>
            <md-card-header data-background-color="orange">
              <h4 class="title">Billing Rules</h4>
              <p class="category">Rules for generating Bills from CDRs</p>
            </md-card-header>
          <md-table v-model="billrules" :table-header-color="tableHeaderColor">
            <md-table-row slot="md-table-row" slot-scope="{ item }">
              <md-table-cell md-label="Name">{{ item.name }}</md-table-cell>
              <md-table-cell md-label="Rule"><pre>{{ item.rule }}</pre></md-table-cell>
              <md-table-cell md-label="Delete">
                <md-button class="md-just-icon md-simple md-danger" v-on:click="deleteRule('bill',item.name)" id="delete">
                  <md-icon>close</md-icon>
                  <md-tooltip md-direction="top">Delete</md-tooltip>
                </md-button>
              </md-table-cell>
            </md-table-row>
          </md-table>
        </md-card>
      </md-tab>
      <md-tab id="add-rule" md-label="New Rule" md-icon="add">
        <md-card>
          <md-card-header data-background-color="orange">
            <h4 class="title">Add Rule</h4>
            <p class="category">Add rules to pricing models</p>
          </md-card-header>
          <md-card-content>
            <addrule table-header-color="green"></addrule>
          </md-card-content>
        </md-card>
      </md-tab>
</md-tabs>
</template>
</nav-tabs-card>
  </div>

</template>

<script>
import {
  NavTabsCard,
  Addrule
} from "./";
export default {
  components:{
    NavTabsCard,
    Addrule
  },
  name: "listrules",
  props: {
    tableHeaderColor: {
      type: String,
      default: ""
    }
  },
  data() {
    return {
      info: null,
      cdrrules: [],
      billrules: [],
      data: {
        "target": "",
        "rule": ""
      }
    };
  },
  mounted(){
      this.updateList();
      this.$root.$on('list', () => {
        this.updateList();
      })

  },
  methods:{
    deleteRule: function(target,rule){
      this.data.target = target
      this.data.rule = rule
      this.$axios
        .post('http://127.0.0.1:5000/removerule',this.data)
        .then(response => {
          this.info = response.data;
          this.$root.$emit('list');
        })
    },
    updateList: function(){
      this.cdrrules = []
      this.billrules = []
      this.$axios
        .get('http://127.0.0.1:5000/listcdrrules')
        .then(response => {
          this.info = response.data;
          for (var key in this.info){
            this.cdrrules.push({name:key,rule:this.info[key]})
          }
        })
      this.$axios
        .get('http://127.0.0.1:5000/listbillrules')
        .then(response => {
          this.info = response.data;
          for (var key in this.info){
            this.billrules.push({name:key,rule:this.info[key]})
          }
        })

    }
  }
};
</script>
