<template>
  <div class="content">
    <div class="md-layout">
      <div
        class="md-layout-item md-medium-size-100 md-xsmall-size-100 md-size-33"
      >

            <forecast @update="onUpdate" table-header-color="green"></forecast>
            <cleanup @update="onUpdate" table-header-color="green"></cleanup>
            <md-card>

</md-card>
      </div>
      <div v-if="totaltotal != 0"
        class="md-layout-item md-medium-size-100 md-xsmall-size-100 md-size-33"
        >
        <md-card :key="componentKey"
          data-background-color="blue"
        >
        <md-card-header data-background-color="blue"><h4 class="title">Revenue by Account</h4></md-card-header>
        <GChart
          type="ColumnChart"
          :data="chartData"
          :options="chartOptions"
          />

          <md-card>

          <md-table v-model="bills" :key="componentKey" :table-header-color="tableHeaderColor">
            <md-table-row slot="md-table-row" slot-scope="{ item }">
              <md-table-cell md-label="Account">{{ item.account.split("-forecast-")[0]}}</md-table-cell>
              <md-table-cell md-label="Model">{{ item.account.split("-forecast-")[1]}}</md-table-cell>
              <md-table-cell md-label="Charge">{{ item.charge }}</md-table-cell>
              <!--<md-table-cell md-label="%">{{ item.percentage }}</md-table-cell> -->
            </md-table-row>
          </md-table>


</md-card>
      </md-card>

          <md-card >
            <md-card-header data-background-color="purple">
            <h4 class="title">Total Forecast by model</h4>
          </md-card-header>
            <GChart
              type="BarChart"
              :data="totalchartData"
              :options="totalchartOptions"
              />
              <md-card>

              <md-table v-model="modeltotal" :key="componentKey" :table-header-color="tableHeaderColor">
                <md-table-row slot="md-table-row" slot-scope="{ item }">
                  <md-table-cell md-label="Model">{{ item.model }}</md-table-cell>
                  <md-table-cell md-label="Revenue">{{ item.charge.toFixed(2) + ' CHF' }}</md-table-cell>
                  <!--<md-table-cell md-label="%">{{ item.percentage }}</md-table-cell> -->
                </md-table-row>
              </md-table>
            </md-card>
          </md-card>

      </div>
      <div v-if="totaltotal != 0"
        class="md-layout-item md-medium-size-100 md-xsmall-size-100 md-size-33"
      >
      <md-card>
        <md-card-header data-background-color="orange">
        <h4 class="title">Bill Breakdowns</h4>
      </md-card-header>
      <md-card v-for="metric in metrics">
        <md-card-header data-background-color="blue">
        <h4 class="title">{{"Account: " + metric.account.split("-forecast-")[0]}}</h4>
        <h4 class="title">{{"Model: " + metric.account.split("-forecast-")[1]}}</h4>
      </md-card-header>
      <md-table v-model="metric.data" :key="componentKey" :table-header-color="tableHeaderColor">
        <md-table-row v-if="item.data.usage!=0" slot="md-table-row" slot-scope="{ item }">
          <md-table-cell md-label="Metric">{{ item.metric }}</md-table-cell>
          <md-table-cell md-label="Usage">{{ item.data.usage.toFixed(0) + " " + item.data.unit + "*h"}}</md-table-cell>
          <md-table-cell md-label="Charge">{{ item.charge.toFixed(2) + " CHF" }}</md-table-cell>
        </md-table-row>
      </md-table>
    </md-card>
    
    </md-card>

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
    chartData: [
        ['Account','Charge']

      ],
      chartOptions: {
        chart: {
          title: 'Revenue Chart',
          subtitle: '',
        },
        legend: 'none',
        colors: ['orange'],


      },
      totalchartData: [
          ['Model','Charge']

        ],
        totalchartOptions: {
          chart: {
            title: 'Revenue Chart',
            subtitle: ''
          },
          legend: 'none',
          colors: ['orange'],

          hAxis: {
              viewWindowMode:'explicit',
              viewWindow:{
                min:0
              }
            }
        },
    componentKey: 0,
    info: null,
    bills: [],
    total: [],
    totaltotal: 0,
    modeltotal: [],
    metrics: [],
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
    }
  }
},
mounted(){
  this.onUpdate();
},
methods:{
  onUpdate(){
    this.bills = []
    this.metrics = []
    this.total = []
    this.totaltotal = 0
    this.modeltotal = []
    this.billchart.data = {
      labels: [],
      series: []
    },
    this.chartData = [
        ['Account','Charge']

      ],
      this.totalchartData = [
          ['Account','Charge']

        ]

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
              this.chartData.push([obj['account'].split('forecast-')[0]+'-'+obj['account'].split('forecast-')[1],obj['charge']]);
              this.total.push({"account":obj['account'],"charge":obj['charge']});
              this.totaltotal += obj['charge'];
              this.bills.push({account:obj['account'],charge:parseFloat(obj['charge']),percentage:0});
              this.metrics.push(obj);
            }
            //tmp.push(obj['charge']);
          }
          for (var i=0; i<this.bills.length; i++){
            //this.bills[i].percentage = ((this.bills[i].charge/this.total)*100).toFixed(2) + '%';
            this.bills[i].charge = this.bills[i].charge.toFixed(2) + " CHF";
          }

          //this.total = this.total.toFixed(2) + " CHF"
          var flag = 0
          for(const bill of this.total){
            if(this.modeltotal.length == 0){
              this.modeltotal.push({"model":bill['account'].split('forecast-')[1],"charge":bill['charge']})
            }
            else{
              for(const model of this.modeltotal){
                if(bill['account'].split('forecast-')[1] == model['model']){
                  model['charge'] += bill['charge']
                  flag = 0
                }
                else{
                  flag = 1
                }
              }
              if(flag == 1){
                this.modeltotal.push({"model":bill['account'].split('forecast-')[1],"charge":bill['charge']})
                flag = 0
              }
            }
          }
          for(const row of this.modeltotal){
            this.totalchartData.push([row.model,row.charge])
          }
          this.componentKey += 1;
          //this.billchart.data.series.push(tmp);
        })
  }
}
};
</script>
