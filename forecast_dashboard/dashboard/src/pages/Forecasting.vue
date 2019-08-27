<template>
  <div class="content">
    <div class="md-layout">
      <div
        class="md-layout-item md-medium-size-100 md-xsmall-size-100 md-size-30"
      >

            <forecast @update="onUpdate" table-header-color="green"></forecast>

      </div>
      <div v-if="total != '0.00 CHF'"
        class="md-layout-item md-medium-size-100 md-xsmall-size-100 md-size-35"
        >
        <chart-card :key="componentKey"
          :chart-data="billchart.data"
          :chart-options="billchart.options"
          :chart-responsive-options="billchart.responsiveOptions"
          :chart-type="'Pie'"
          data-background-color="blue"
        >
        <template slot="content">
          <h4 class="title">Cost by account</h4>
          <md-table v-model="bills" :key="componentKey" :table-header-color="tableHeaderColor">
            <md-table-row slot="md-table-row" slot-scope="{ item }">
              <md-table-cell md-label="Account">{{ item.account }}</md-table-cell>
              <md-table-cell md-label="Charge">{{ item.charge }}</md-table-cell>
              <md-table-cell md-label="%">{{ item.percentage }}</md-table-cell>
            </md-table-row>
          </md-table>
        </template>


        </chart-card>
          <stats-card :key="componentKey" data-background-color="purple">
            <template slot="header">
              <md-icon>store</md-icon>
            </template>

            <template slot="content">
              <p class="category">Total Forecast</p>
              <h3 class="title">{{total}}</h3>
            </template>
          </stats-card>

      </div>
      <div v-if="total != '0.00 CHF'"
        class="md-layout-item md-medium-size-100 md-xsmall-size-100 md-size-35"
      >
      <md-card>
        <md-card-header data-background-color="blue">
        <h4 class="title">Cost by metric</h4>
      </md-card-header>
        <md-table v-model="metrics" :key="componentKey" :table-header-color="tableHeaderColor">
          <md-table-row slot="md-table-row" slot-scope="{ item }">
            <md-table-cell md-label="Type">{{ item.type }}</md-table-cell>
            <md-table-cell md-label="Usage">{{ item.usage }}</md-table-cell>
            <md-table-cell md-label="Charge">{{ item.charge }}</md-table-cell>
            <md-table-cell md-label="%">{{ item.percentage }}</md-table-cell>
          </md-table-row>
        </md-table>


    </md-card>

      </div>
      <div
        class="md-layout-item md-medium-size-100 md-xsmall-size-100 md-size-70"
      >

            <cleanup @update="onUpdate" table-header-color="green"></cleanup>

      </div>
  </div>

    </div>

  </div>

</template>

<script>
import { Forecast, ChartCard, StatsCard, Cleanup } from "@/components";

export default {
  components: {
    Forecast,
    ChartCard,
    StatsCard,
    Cleanup
  },
data(){
  return{
    componentKey: 0,
    info: null,
    bills: [],
    total: 0,
    metrics: [
      {
        type:"Memory Used Limit",
        usage:0,
        charge:0,
        percentage:0
      },
      {
        type:"CPU Used Limit",
        usage:0,
        charge:0,
        percentage:0
      },
      {
        type:"Service External IP count",
        usage:0,
        charge:0,
        percentage:0
      },
      {
        type:"Storage Allocated",
        usage:0,
        charge:0,
        percentage:0
      },
      {
        type:"Storage Requested",
        usage:0,
        charge:0,
        percentage:0
      },
      {
        type:"Memory Limit",
        usage:0,
        charge:0,
        percentage:0
      },
      {
        type:"CPU Limit",
        usage:0,
        charge:0,
        percentage:0
      },
      {
        type:"Service Port Count",
        usage:0,
        charge:0,
        percentage:0
      },
    ],
    billchart: {
      data: {
        labels: [],
        series: []
      },
      options: {
        labelInterpolationFnc: function(value) {
        return value.substring(0,4)
      }
      },
      responsiveOptions: []
    },
    metricchart: {
      data: {
        labels: ['RAM','CPU','IP','SA','SR','P','RAM_A','CPU_A'],
        series: [0,0,0,0,0,0,0,0]
      },
      options: {
      },
      responsiveOptions: []
    }
  }
},
mounted(){
  this.onUpdate();
},
methods:{
  onUpdate(){
    this.bills = []
    this.metrics = [
      {
        type:"Memory Used Limit",
        usage:0,
        charge:0,
        percentage:0
      },
      {
        type:"CPU Used Limit",
        usage:0,
        charge:0,
        percentage:0
      },
      {
        type:"Service External IP count",
        usage:0,
        charge:0,
        percentage:0
      },
      {
        type:"Storage Allocated",
        usage:0,
        charge:0,
        percentage:0
      },
      {
        type:"Storage Requested",
        usage:0,
        charge:0,
        percentage:0
      },
      {
        type:"Memory Limit",
        usage:0,
        charge:0,
        percentage:0
      },
      {
        type:"CPU Limit",
        usage:0,
        charge:0,
        percentage:0
      },
      {
        type:"Service Port Count",
        usage:0,
        charge:0,
        percentage:0
      },
    ]
    this.total = 0
    this.billchart.data = {
      labels: [],
      series: []
    }
    this.metricchart.data.series = [0,0,0,0,0,0,0,0]
    this.$axios
        .get('http://127.0.0.1:5000/bills')
        .then(response => {
          this.info = response.data.data;
          //var tmp = [];
          for (var i=0; i<this.info.length; i++){
            var obj = this.info[i];
            var string = obj['account'];
            var regex = /forecast/;
            if(regex.test(string)){
              this.billchart.data.labels.push(obj['account']);
              this.billchart.data.series.push(obj['charge']);
              this.total += parseFloat(obj['charge']);
              this.bills.push({account:obj['account'],charge:parseFloat(obj['charge']),percentage:0});
              for (var j=0; j<obj['data'].length;j++){
                var record = obj['data'][j];
                if(record['metric']==='cpu'){
                  this.metrics[1].usage+=record['data']['usage'];
                  this.metrics[1].charge+=record['charge'];
                  this.metricchart.data.series[1]+=record['charge'];
                }
                if(record['metric']==='memory'){
                  this.metrics[0].usage+=record['data']['usage'];
                  this.metrics[0].charge+=record['charge'];
                  this.metricchart.data.series[0]=record['charge'];
                }
                if(record['metric']==='ip'){
                  this.metrics[2].usage+=record['data']['usage'];
                  this.metrics[2].charge+=record['charge'];
                  this.metricchart.data.series[2]+=record['charge'];
                }
                if(record['metric']==='storage_allocated'){
                  this.metrics[3].usage+=record['data']['usage'];
                  this.metrics[3].charge+=record['charge'];
                  this.metricchart.data.series[3]+=record['charge'];
                }
                if(record['metric']==='storage_requested'){
                  this.metrics[4].usage+=record['data']['usage'];
                  this.metrics[4].charge+=record['charge'];
                  this.metricchart.data.series[4]+=record['charge'];
                }
                if(record['metric']==='memory_limit'){
                  this.metrics[5].usage+=record['data']['usage'];
                  this.metrics[5].charge+=record['charge'];
                  this.metricchart.data.series[5]+=record['charge'];
                }
                if(record['metric']==='cpu_limit'){
                  this.metrics[6].usage+=record['data']['usage'];
                  this.metrics[6].charge+=record['charge'];
                  this.metricchart.data.series[6]+=record['charge'];
                }
                if(record['metric']==='port'){
                  this.metrics[7].usage+=record['data']['usage'];
                  this.metrics[7].charge+=record['charge'];
                  this.metricchart.data.series[7]+=record['charge'];
                }
              }
            }
            //tmp.push(obj['charge']);
          }
          for (var i=0; i<this.bills.length; i++){
            this.bills[i].percentage = ((this.bills[i].charge/this.total)*100).toFixed(2) + '%';
            this.bills[i].charge = this.bills[i].charge.toFixed(2) + " CHF";
          }
          for (var i=0; i<this.metrics.length; i++){
            this.metrics[i].percentage = ((this.metrics[i].charge/this.total)*100).toFixed(2) + '%';
            this.metrics[i].charge = this.metrics[i].charge.toFixed(2) + " CHF";
          }
          this.metrics[0].usage = (this.metrics[0].usage/1000000000).toFixed(0) + ' GB*h';
          this.metrics[1].usage = (this.metrics[1].usage/1000).toFixed(0) + ' KCU*h';
          this.metrics[2].usage = (this.metrics[2].usage).toFixed(0) + ' IP*h';
          this.metrics[3].usage = (this.metrics[3].usage/1000000000).toFixed(0) + ' GB*h';
          this.metrics[4].usage = (this.metrics[4].usage/1000000000).toFixed(0) + ' GB*h';
          this.metrics[5].usage = (this.metrics[5].usage/1000000000).toFixed(0) + ' GB*h';
          this.metrics[6].usage = (this.metrics[6].usage/1000).toFixed(0) + ' KCU*h';
          this.metrics[7].usage = (this.metrics[7].usage).toFixed(0) + ' Port*h';
          this.total = this.total.toFixed(2) + " CHF"
          this.componentKey += 1;
          //this.billchart.data.series.push(tmp);
        })
  }
}
};
</script>
