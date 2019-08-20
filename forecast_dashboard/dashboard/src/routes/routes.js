import DashboardLayout from "@/pages/Layout/DashboardLayout.vue";


import RuleMgmt from "@/pages/RuleMgmt.vue";
import Forecasting from "@/pages/Forecasting.vue"

const routes = [
  {
    path: "/",
    component: DashboardLayout,
    redirect: "/forecast",
    children: [
      
      {
        path: "rulemgmt",
        name: "Rule Management",
        component: RuleMgmt
      },
      {
        path: "forecast",
        name: "Forecasting",
        component: Forecasting
      }
    ]
  }
];

export default routes;
