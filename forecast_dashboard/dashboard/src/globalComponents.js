import DropDown from "./components/Dropdown.vue";
import ChartCard from "./components/Cards/ChartCard.vue"

/**
 * You can register global components here and use them as a plugin in your main Vue instance
 */

const GlobalComponents = {
  install(Vue) {
    Vue.component("drop-down", DropDown);
    Vue.component("chart-card", ChartCard);
  }
};

export default GlobalComponents;
