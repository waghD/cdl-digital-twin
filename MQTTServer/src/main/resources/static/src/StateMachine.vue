<style scoped>
.panel {
  display: block;
  overflow: hidden;
}

.accordion {
  cursor: pointer;
  transition: 0.4s;
}

.state-machine {
  font-size: 0.8em;
  /*min-height: 50vh;*/
  margin-bottom: 20px;
}

.states-container {
  border: 3px solid;
  border-radius: 5px;
  padding: 0px 20px;
}
span.state-machine-header {
  font-size: 1.6rem;
  color: white;
}

.robotic-arm-state-machine span.state-machine-header {
  background-color: #1e88e5;
  padding: 2px 5px;
}

.robotic-arm-state-machine .machine {
  border: 3px solid #1e88e5;
  padding: 0px 20px;
  border-radius: 3px;
}

.slider-state-machine span.state-machine-header {
  background-color: orange;
  padding: 2px 5px;
}

.slider-state-machine .machine {
  border: 3px solid orange;
  padding: 0px 20px;
  border-radius: 3px;
}

.testrig-state-machine span.state-machine-header {
  background-color: darkgray;
  padding: 2px 5px;
}

.testrig-state-machine .machine {
  border: 3px solid darkgray;
  padding: 0px 20px;
  border-radius: 3px;
}

.conveyor-state-machine span.state-machine-header {
  background-color: darkgreen;
  padding: 2px 5px;
}

.conveyor-state-machine .machine {
  border: 3px solid darkgreen;
  padding: 0px 20px;
  border-radius: 3px;
}

.dim {
  position: fixed;
  padding: 0;
  margin: 0;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 10;
}

.popup {
  position: fixed;
  padding: 20px;
  top: 50%;
  left: 50%;
  width: 40vw;
  height: 50vh;
  background: white;
  border-radius: 10px;
  z-index: 11;
  transform: translate(-50%, -50%);
}

.new {
  display: inline-block;
  border: 0px solid white;
  border-radius: 5px;
  box-shadow: 1px 1px 10px grey;
  padding: 1px;
  text-align: center;
  font-size: 0.6em;
}
.machine-header {
  color: white;
  font-size: 1.4rem;
  padding: 5px 10px;
  border-radius: 3px;
}
.new i {
  font-size: 4em;
  color: darkslategray;
}

.row {
  display: inline-block;
  vertical-align: top;
}
</style>
<template>
  <div class="state-machine">
    <span
      class="machine-header accordion"
      :style="titleColor"
      @click="accordionHandler"
      >{{ machineName }}</span
    >

    <div class="states-container panel" :style="borderColor">
      <div v-if="showPopup" class="dim">
        <div class="popup">
          <div v-if="selectedState && !showChoiceSettings">
            <state-detail
              :state="selectedState"
              :stateGroup="stateGroup"
              :socket="socket"
              :context="context"
              :possibleDependecies="possibleDependencyStates"
              v-on:close="showPopup = false"
              v-on:recordPosition="saveChanges()"
            ></state-detail>
          </div>
          <div v-if="showChoiceSettings">
            <choice-settings
              :state="selectedState"
              :followupState="followupState"
              v-on:recordPosition="saveChanges()"
              v-on:close="
                showPopup = false;
                showChoiceSettings = false;
              "
            ></choice-settings>
          </div>
        </div>
      </div>
      <div>
        <div v-for="(s, index) in job[stateGroup]" class="row">
          <div v-if="!s.choices" style="width: 260px; padding-top: 75px">
            <state-preview
              v-on:open="showState(s)"
              :state="s"
              :index="index"
              :active="s.active"
              :context="context"
              :socket="socket"
              v-on:moveLeft="moveLeft($event)"
              v-on:moveRight="moveRight($event)"
              v-on:remove="remove($event)"
              v-on:recordPosition="saveChanges()"
            >
            </state-preview>
            <div
              style="
                color: darkslategray;
                display: inline-block;
                vertical-align: top;
                margin-left: 20px;
              "
            >
              <i
                @click="toggleChoice(index)"
                v-if="
                  !s.choices &&
                  index < job[stateGroup].length - 1 &&
                  !job[stateGroup][index + 1].choices
                "
                class="material-icons"
                style="font-size: 3em; position: relative; top: 45px"
                >arrow_right_alt</i
              >
              <div
                style="display: inline-block; vertical-align: top"
                v-if="
                  index < job[stateGroup].length - 1 &&
                  job[stateGroup][index + 1].choices
                "
              >
                <div>
                  <i
                    @click="choiceSettings(s, index)"
                    class="material-icons"
                    style="
                      cursor: default;
                      font-size: 2em;
                      position: relative;
                      top: 15px;
                      left: 5px;
                    "
                    >info</i
                  >
                </div>
                <div>
                  <i
                    @click="toggleChoice(index)"
                    class="material-icons"
                    style="
                      cursor: default;
                      transform: rotate(90deg);
                      font-size: 3em;
                      position: relative;
                      top: 15px;
                    "
                    >call_split</i
                  >
                </div>
              </div>
            </div>
          </div>
          <div v-if="s.choices">
            <div
              style="
                display: inline-block;
                vertical-align: top;
                margin-right: -40px;
              "
            >
              <div>
                <div
                  v-for="(s1, i1) in s.choices.first"
                  style="
                    display: inline-block;
                    vertical-align: top;
                    width: 260px;
                  "
                >
                  <state-preview
                    v-on:open="showState(s1)"
                    :state="s1"
                    :index="i1"
                    :active="s1.active"
                    :context="context"
                    :socket="socket"
                    v-on:moveLeft="moveLeftChoice(s.choices.first, $event)"
                    v-on:moveRight="moveRightChoice(s.choices.first, $event)"
                    v-on:remove="
                      removeChoice(s, s.choices.first, $event, index)
                    "
                    v-on:recordPosition="saveChanges()"
                  >
                  </state-preview>
                  <div
                    v-if="i1 < s.choices.first.length - 1"
                    style="
                      display: inline;
                      position: relative;
                      left: 20px;
                      top: 44px;
                    "
                  >
                    <i
                      class="material-icons"
                      style="font-size: 3em; color: darkslategrey"
                      >arrow_right_alt</i
                    >
                  </div>
                  <button
                    class="new"
                    style="position: relative; top: 45px; left: 20px"
                    v-if="
                      i1 == s.choices.first.length - 1 &&
                      index == job[stateGroup].length - 1
                    "
                    @click="addStateChoice(s.choices.first)"
                  >
                    <i class="material-icons">add</i>
                  </button>
                </div>
              </div>
              <div>
                <div
                  v-for="(s2, i2) in s.choices.second"
                  style="
                    display: inline-block;
                    vertical-align: top;
                    width: 260px;
                  "
                >
                  <state-preview
                    v-on:open="showState(s2)"
                    :state="s2"
                    :index="i2"
                    :active="s2.active"
                    :context="context"
                    :socket="socket"
                    v-on:moveLeft="moveLeftChoice(s.choices.second, $event)"
                    v-on:moveRight="moveRightChoice(s.choices.second, $event)"
                    v-on:remove="
                      removeChoice(s, s.choices.second, $event, index)
                    "
                    v-on:recordPosition="saveChanges()"
                  >
                  </state-preview>
                  <div
                    v-if="i2 < s.choices.second.length - 1"
                    style="
                      display: inline;
                      position: relative;
                      left: 20px;
                      top: 44px;
                    "
                  >
                    <i
                      class="material-icons"
                      style="font-size: 3em; color: darkslategrey"
                      >arrow_right_alt</i
                    >
                  </div>
                  <button
                    class="new"
                    style="position: relative; top: 45px; left: 20px"
                    v-if="
                      i2 == s.choices.second.length - 1 &&
                      index == job[stateGroup].length - 1
                    "
                    @click="addStateChoice(s.choices.second)"
                  >
                    <i class="material-icons">add</i>
                  </button>
                </div>
              </div>
            </div>
            <div
              style="
                display: inline-block;
                position: relative;
                top: 115px;
                left: -20px;
              "
            >
              <i
                class="material-icons"
                style="
                  font-size: 34px;
                  color: darkslategrey;
                  transform: rotate(90deg);
                "
                >call_merge</i
              >
            </div>
          </div>
        </div>
        <button @click="addState()" class="new" style="margin-top: 120px">
          <i class="material-icons">add</i>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import StatePreview from "./StatePreview.vue";
import StateDetail from "./StateDetail.vue";
import ChoiceSettings from "./ChoiceSettings.vue";
import Vue from "vue";

export default {
  props: ["job", "stateGroup", "color", "machineName", "context", "socket"],
  computed: {
    titleColor() {
      return "background-color: " + this.color;
    },
    borderColor() {
      return "border-color: " + this.color;
    },
  },
  components: {
    statePreview: StatePreview,
    stateDetail: StateDetail,
    choiceSettings: ChoiceSettings,
  },
  data() {
    return {
      editName: false,
      showPopup: false,
      showChoiceSettings: false,
      selectedState: null,
      followupState: null,
      selectedTransition: null,
      possibleDependencyStates: [],
    };
  },

  watch: {
    job(val) {},

    
  },

  created() {
    const possibleDependencies = [];

    var count = 0;
    for (const key in this.job) {
      if (
        key !== this.stateGroup &&
        this.job[key] instanceof Array &&
        this.job.hasOwnProperty(key)
      ) {
        const stateNames = this.job[key].map((state) => state.name);
        let states = [];
        stateNames.forEach((sName) => {
          states.push({
            name: sName,
            index: count++,
          });
        });

        possibleDependencies.push({
          stateGroup: key,
          states: states,
        });
      }
    }

    possibleDependencies.push({
      stateGroup: "cameraStates",
      states: [
        {
          name: "testRigDetected",
          index: count++,
        },
        {
          name: "pickupDetected",
          index: count++,
        },
      ],
    });

    console.log("possible Dependencies: ", possibleDependencies);

    this.possibleDependencyStates = possibleDependencies;
  },

  mounted() {
    var self = this;

    self.job[self.stateGroup][0].active = true;

    this.socket.addEventListener("message", function (event) {
      let msg = JSON.parse(event.data);

      if (msg.topic === "state") {
        let name = JSON.parse(msg.message).name;

        // Grab relevant states for this machinery by checking with stateGroup
        let groupStatesNames = [];
        self.job[self.stateGroup].forEach((state) => {
          groupStatesNames.push(state.name);
        });

        if (groupStatesNames.includes(name)) {
          let jobs = [];
          for (let j of self.job[self.stateGroup]) {
            if (j.type === "BasicState") {
              jobs.push(j);
            } else {
              jobs = jobs.concat(j.choices.first);
              jobs = jobs.concat(j.choices.second);
            }
          }

          jobs.forEach(function (x) {
            if (x.name === name) {
              x.active = true;
            } else {
              x.active = false;
            }
            self.$forceUpdate();
          });
        }
      }
    });
    self.$forceUpdate();
  },
  methods: {
    accordionHandler(e) {
      let panel = e.target.nextElementSibling;
      if (panel.style.display === "block") {
        panel.style.display = "none";
      } else {
        panel.style.display = "block";
      }
    },

    toggleChoice(index) {
      var state = this.job[this.stateGroup][index + 1];
      var prevState = this.job[this.stateGroup][index];
      if (!state.choices) {
        state.type = "ChoiceState";
        var choices = {
          first: [],
          second: [],
        };
        if (prevState.environment.testingRigState == null) {
          prevState.environment.testingRigState = {
            objectCategory: "GREEN",
          };
        }
        prevState.altEnvironment = JSON.parse(
          JSON.stringify(prevState.environment)
        ); // copy constructor
        prevState.environment.testingRigState.objectCategory = "GREEN";
        prevState.altEnvironment.testingRigState.objectCategory = "RED";
        prevState.sensor = "qr";
        state.choices = choices;
        Vue.set(state, "choices", choices);
        state.choices.first.push({
          name: state.name,
          type: "BasicState",
          environment: state.environment,
        });
        state.choices.second.push({
          name: "New " + (this.job[this.stateGroup].length + 1),
          type: "BasicState",
          environment: Object.assign({}, state.environment),
        });
        delete state.environment;
      } else if (
        state.choices.first.length == 1 &&
        state.choices.second.length == 1
      ) {
        state.name = state.choices.first[0].name;
        state.type = state.choices.first[0].type;
        state.environment = state.choices.first[0].environment;
        delete state.choices;
        state.type = "BasicState";
      }
      this.$forceUpdate();
    },
    choiceSettings(s, i) {
      this.selectedState = s;
      this.followupState = this.job[this.stateGroup][i + 1];
      this.showPopup = true;
      this.showChoiceSettings = true;
    },
    addState() {
      let newStateName = "New " + (this.job[this.stateGroup].length + 1)
      let newStateObj = {
        name: newStateName,
        type: "BasicState",
        environment: {
          roboticArmState: null,
          conveyorState: null,
          testingRigState: null,
          sliderState: null
        },
        dependencies: {

        }
      };
      //Initial values acc. to statemachine type
      switch(this.stateGroup) {
        case "roboticArmStates":
          newStateObj.environment.roboticArmState = {
             "basePosition": 0.0,
              "mainArmPosition": 0.0,
              "secondArmPosition": 0.0,
              "headPosition": 0.0,
              "headMountPosition": 0.0,
              "gripperPosition": 0.0
          }
          break
        case "sliderStates":
          newStateObj.environment.sliderState = {
            "sliderPosition": 0.078
          }
          break
        case "conveyorStates":
          newStateObj.environment.conveyorState = {
            "adjusterPosition": 1.67
          }
          break
        case "testingRigStates":
          newStateObj.environment.testingRigState = {
            "heatplateTemperature": 110.0
          }
          break
      }
      this.job[this.stateGroup].push(newStateObj);
      this.$emit('rerenderCB');
    },
    addStateChoice(s) {
      let newStateObj = 
      {
        name: "New " + (s.length + 1),
        type: "ChoiceState",
        environment: {
          roboticArmState: null,
          conveyorState: null,
          testingRigState: null,
          sliderState: null
        }
      };
//Initial values acc. to statemachine type
      switch(this.stateGroup) {
        case "roboticArmStates":
          newStateObj.environment.roboticArmState = {
             "basePosition": 0.0,
              "mainArmPosition": 0.0,
              "secondArmPosition": 0.0,
              "headPosition": 0.0,
              "headMountPosition": 0.0,
              "gripperPosition": 0.0
          }
          break
        case "sliderStates":
          newStateObj.environment.sliderState = {
            "sliderPosition": 0.078
          }
          break
        case "conveyorStates":
          newStateObj.environment.conveyorState = {
            "adjusterPosition": 1.67
          }
          break
        case "testingRigStates":
          newStateObj.environment.testingRigState = {
            "heatplateTemperature": 110.0
          }
          break
      }
      s.push(newStateObj)
      this.$forceUpdate();
    },

    moveLeft(i) {
      event.stopPropagation();
      if (i - 1 < 0) return;
      var elem = this.job[this.stateGroup][i];
      var right = this.job[this.stateGroup][i - 1];
      this.job[this.stateGroup].splice(i, 1, right);
      this.job[this.stateGroup].splice(i - 1, 1, elem);
      this.$emit("changes");
      this.saveChanges();
    },
    moveLeftChoice(s, i) {
      event.stopPropagation();
      if (i - 1 < 0) return;
      var elem = s[i];
      var right = s[i - 1];
      s.splice(i, 1, right);
      s.splice(i - 1, 1, elem);
      this.$emit("changes");
      this.saveChanges();
      this.$forceUpdate();
    },
    moveRight(i) {
      event.stopPropagation();
      if (i + 1 > this.job[this.stateGroup].length - 1) return;
      var elem = this.job[this.stateGroup][i];
      var right = this.job[this.stateGroup][i + 1];
      this.job[this.stateGroup].splice(i, 1, right);
      this.job[this.stateGroup].splice(i + 1, 1, elem);
      this.$emit("changes");
      this.saveChanges();
    },
    moveRightChoice(s, i) {
      event.stopPropagation();
      if (i + 1 > s.length - 1) return;
      var elem = s[i];
      var right = s[i + 1];
      s.splice(i, 1, right);
      s.splice(i + 1, 1, elem);
      this.$emit("changes");
      this.saveChanges();
      this.$forceUpdate();
    },
    remove(i) {
      event.stopPropagation();
      this.job[this.stateGroup].splice(i, 1);
      this.$emit("changes");
      this.saveChanges();
    },
    removeChoice(s, choices, localIndex, globalIndex) {
      event.stopPropagation();
      choices.splice(localIndex, 1);
      if (choices.length === 0) {
        if (s.choices.first.length === 0) {
          var c = s.choices.second;
        } else {
          var c = s.choices.first;
        }
        this.job[this.stateGroup].splice(globalIndex, 1);
        for (var a = 0; a < c.length; a++) {
          this.job[this.stateGroup].splice(globalIndex + a, 0, c[a]);
        }
      }

      this.$emit("changes");
      this.saveChanges();
      this.$forceUpdate();
    },
    saveChanges() {
      var xhr = new XMLHttpRequest();
      xhr.open("PUT", "http://localhost:8080/jobs/" + this.job.id, true);
      xhr.setRequestHeader("Content-type", "application/json; charset=utf-8");
      xhr.send(JSON.stringify(this.job));
    },
    showState(s) {
      this.showPopup = true;
      this.selectedTransition = null;
      this.selectedState = s;
    },
    showTransition(index) {
      this.showPopup = true;
      var first = this.job[this.stateGroup][index];
      var second = this.job[this.stateGroup][index + 1];
      this.selectedState = null;
      this.selectedTransition = {
        first: first,
        second: second,
      };
    },
  },
};
</script>
