<style scoped>
h1 {
    margin-top: 0px;
    margin-bottom: 0px;
}

p {
    font-size: 11px;
    line-height: 30px;
    margin-bottom: 0px;
}

input {
    width: 45px;
    float: right;
}

.third {
    width: 28%;
    padding: 2%;
    display: inline-block;
    vertical-align: top;
    height: 100%;
}

.attribute {
    font-family: 'Roboto Mono' monospace;
}

.disabled {
    opacity: 0.5;
}

.card-title {
  position: relative;
}

.toggleButton {
  position: absolute;
  top: 50%;
  right: 0;
  transform: translateY(-50%);
}

.bottom-btn-container {
  position: absolute;
  height: auto;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
}

.main-card-area {
  height: calc(50vh - 42px - 40px - 55px);
  width: 100%;
}

.dependency-row {
  width: 100%;
  position: relative;
  height: 25px;
  padding: 8px;
  display: flex;
  align-items: center;
}

.dependency-row .material-icons {
    margin-left: 10px;
}

.dependency-select {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 50%;
}

.dependency-type-select {
  position: absolute;
  top: 50%;
  left: 55%;
  transform: translateY(-50%);
  width: 20%;
}

.dependency-add-btn {
  position: absolute;
  top: 50%;
  right: 0;
  transform: translateY(-50%);
}

.dependency-row i:hover {
  cursor:pointer;
  opacity:0.6;
}

</style>
<template>
<div>
  <div class="card-title">
    <h1>{{state.name}}</h1>
    <button class="toggleButton" @click="showDependecies = !showDependecies">Toggle Dependencies</button>
  </div>
  <div class="main-card-area" v-if="!showDependecies">
    <!--
    <div class="third" v-if="stateGroup == 'testingRigStates'">
      <h2>Testing Rig <input type="checkbox" v-model="testingRig"></h2>
      <div v-bind:class="{ disabled: !testingRig}">
        <h3>Sensors</h3>
        <p>QR Code:
          <select style="float: right;" v-model="state.environment.testingRigState.objectCategory" v-if="testingRig">
            <option value="NONE">None</option>
            <option value="GREEN">Class 1</option>
            <option value="RED">Class 2</option>
          </select>
        </p>
        <h3>Actuators</h3>
        <p>Plate Temp.: <input type="number" step="0.1" style="width: 45px;" v-if="testingRig" v-model="state.environment.testingRigState.heatplateTemperature">°C</p>
      </div>
    </div>
    -->

    <div class="third" v-if="stateGroup == 'sliderStates'">
      <h2>Slider</h2>
        <div v-bind:class="{ disabled: !slider}">
          <h3>Actuators</h3>
          <div v-if="state.environment.sliderState">
            <p>Position: <input v-model="state.environment.sliderState.sliderPosition" type="number" step="0.01"></p>
            <button @click="move()">Move to Position</button>
          </div>
        </div>
    </div>
    <div class="third" v-if="stateGroup == 'roboticArmStates'">
      <h2>Robotic Arm</h2>
      <div v-bind:class="{ disabled: !roboticArm}">
        <h3>Actuators</h3>
        <div v-if="state.environment.roboticArmState">
          <p>Base: <input v-model="state.environment.roboticArmState.basePosition" type="number" step="0.01"></p>
          <p>Main Arm: <input v-model="state.environment.roboticArmState.mainArmPosition" type="number" step="0.01"></p>
          <p>Second Arm: <input v-model="state.environment.roboticArmState.secondArmPosition" type="number" step="0.01"></p>
          <p>Head: <input v-model="state.environment.roboticArmState.headPosition" type="number" step="0.01"></p>
          <p>Head Mount: <input v-model="state.environment.roboticArmState.headMountPosition" type="number" step="0.01"></p>
          <p>Gripper: <input v-model="state.environment.roboticArmState.gripperPosition" type="number" step="0.01"></p>
          <button @click="move()">Move to Position</button>
        </div>
        <div v-if="isEmpty(state.environment)" style="margin-bottom: 10px;">
          <p>Use the <b>arrow keys</b> to move the robot.</p>
          <key-controls :socket="socket"></key-controls>
          <button style="font-size: 1.2em;" @click="savePosition()">Save</button>
        </div>
      </div>
    </div>
    
    <div class="third" v-if="stateGroup == 'conveyorStates'">
      <h2>Conveyor <input type="checkbox" v-model="conveyor"></h2>
      <div v-bind:class="{ disabled: !conveyor}">
        <h3>Actuators</h3>
        <p>Adjuster: <select v-if="conveyor" style="float: right;" v-model="state.environment.conveyorState.adjusterPosition"><option value="1.67">Open</option><option value="1.91">Pushed</option></select></p>
        <p><input readonly style="float: right;" v-model="state.environment.conveyorState.adjusterPosition" type="number" step="0.01"></p><br><br>
        <p>Activate Slider: <input style="float: right;" type="checkbox" v-model="slider"></p>
        <p>Slider: </p>
        <button @click="move()">Move to Position</button>

      </div>
    </div>
  </div>


  <div class="main-card-area" v-if="showDependecies">
    <p>List of dependencies</p>
    <!-- Type coersion with != instead of !== required for null != undefined to be false -->
    <div class="dependency-row" v-for="dependency in Object.entries(this.state.dependencies).filter(entry => entry[1] != undefined)">
     
      <span>{{dependency[0]}}: {{dependency[1]}}</span>
       <i @click="deleteDependency(dependency[0])" class="material-icons">delete_forever</i>
    </div>

    <p>New Dependency</p>
    <div class="dependency-row">
      <select class="dependency-select" @change="onDependencySelectChange($event)" v-model="newDependencyModel">
        <option value="-1">Choose Dependency</option>
        <optgroup v-for="stateCollection in possibleDependecies" :label="stateCollection.stateGroup">
            <option v-for="state in stateCollection.states" v-bind:value="state.index">{{ state.name }}</option>
        </optgroup>

      </select>
      <select class="dependency-type-select" v-model="newDependencyType" v-if="!dependencySelectIsHidden">
        <option>Choose Dependency Type</option>
        <option value="true">Positive</option>
        <option value="false">Negative</option>
      </select>

      <button class="dependency-add-btn" @click="addDependency()">Add Dependency</button>
    </div>
  </div>
    <div class="bottom-btn-container">
      <button @click="save()">Save</button>
      <button @click="close()">Close</button>
    </div>
</div>
</template>

<script>
import KeyControls from './KeyControls.vue'

export default {
    components: {
        keyControls: KeyControls
    },
    props: ["state", "stateGroup", "context", "socket", "possibleDependecies"],
    data() {
        return {
            testingRig: this.state.environment.testingRigState != null,
            roboticArm: this.state.environment.roboticArmState != null,
            conveyor: this.state.environment.conveyorState != null,
            slider: this.state.environment.sliderState != null,
            showDependecies: false,
            currentDependecies: [],
            newDependencyModel: -1,
            newDependencyType: '',
            dependencySelectIsHidden: true,
        }
    },
    watch: {
        state(val) {
          console.log('state whatch: ');
          console.log('val: ', val);
          if(val) {
            let dependencies = [];
            for(const key in val.dependencies) {
              if(val.dependencies.hasOwnProperty(key) && val.dependencies[key] !== undefined) {
                dependencies.push({
                  dependencyState: key,
                  positiveDependecy: val.dependencies[key]
                });
              }
            }
            console.log('dependencies: ', dependencies);
            this.currentDependecies = dependencies;
          } else {
            this.currentDependecies = []
          }
        },
        testingRig(val) {
            if (val) {
                this.state.environment.testingRigState = {
                    heatplateTemperature: null,
                    objectCategory: null
                }
            } else {
                this.state.environment.testingRigState = null
            }
        },
        conveyor(val) {
            if (val) {
                this.state.environment.conveyorState = {
                    detected: null,
                    inPickupWindow: null
                }
            } else {
                this.state.environment.conveyorState = null
            }
        },
        slider(val) {
            if (val) {
                this.state.environment.sliderState = {
                    sliderPosition: null
                }
            } else {
                this.state.environment.sliderState = null
            }
        }
    },
    mounted() {
      console.log('state details mount: ');
      console.log('val: ', this.state);
      if(this.state) {
        let dependencies = [];
        for(const key in this.state.dependencies) {
          if(this.state.dependencies.hasOwnProperty(key) && this.state.dependencies[key] !== undefined) {
            dependencies.push({
              dependencyState: key,
              positiveDependecy: this.state.dependencies[key]
            });
          }
        }
        console.log('dependencies: ', dependencies);
        this.currentDependecies = dependencies;
      } else {
        this.currentDependecies = []
      }
    },
    methods: {
        close() {
            this.$emit('close');
        },
        save() {
            this.$emit('recordPosition');
        },
        isEmpty(env) {
            return env.testingRigState == null &&
                env.roboticArmState == null &&
                env.conveyorState == null
        },
        savePosition() {
            console.log(this.context)
            var context = this.context.roboticArmState
            this.state.environment.roboticArmState = {}
            var r = this.state.environment.roboticArmState
            r.basePosition = context.basePosition.toFixed(2);
            r.mainArmPosition = context.mainArmPosition.toFixed(2);
            r.secondArmPosition = context.secondArmPosition.toFixed(2);
            r.headPosition = context.headPosition.toFixed(2);
            r.headMountPosition = context.headMountPosition.toFixed(2);
            r.gripperPosition = context.gripperPosition.toFixed(2);
            this.$emit('recordPosition');
            this.$forceUpdate();
        },
        move() {
            var xhr = new XMLHttpRequest();
            xhr.open("POST", 'http://localhost:8080/moveEnvironment', true);
            xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
            xhr.send(JSON.stringify(this.state.environment));
        },
        deleteDependency(el) {
          console.log(this.state.dependencies);
          this.state.dependencies[el] = null;
        
          this.$forceUpdate();
        },
        getStateByIndex(index) {

          //Retrieve all possible states to be possible dependencies
          let stateNames = [] 

          this.possibleDependecies.forEach(dependencyGroup => {
              dependencyGroup.states.forEach(state => {
                 stateNames.push(state.name);
              })
          })

          return stateNames[index];

        },
        isSensorDependency(stateToUpdate) {

          //Retrieve camera states to distinguish sensor-dependencies from state-dependencies
          let cameraStateNames = null;

          for (let i = 0; i < this.possibleDependecies.length; i++) {
            if (this.possibleDependecies[i].stateGroup == 'cameraStates') cameraStateNames = this.possibleDependecies[i].states.map(function(el) { return el.name; });
          }

          if (cameraStateNames != null && cameraStateNames.length > 0 && cameraStateNames.includes(stateToUpdate)) return true;

          return false;

        },
        addDependency() {
          if(this.newDependencyModel == -1) return;
          this.state.dependencies = this.state.dependencies || {};   

          

          //Get selected as dependency to add
          let stateToUpdate = this.getStateByIndex(this.newDependencyModel);          

          //If is sensor dependencie, set true or false as selected, otherwise (if state dependency) add to list of state dependencies 
          if (this.isSensorDependency(stateToUpdate)) {
            this.state.dependencies[stateToUpdate] = this.newDependencyType === 'true';
          } else { //is state dependency
            this.state.dependencies.states = this.state.dependencies.states == null ? [stateToUpdate] : [...this.state.dependencies.states, stateToUpdate] 
          }
          
          this.newDependencyModel = -1;
          this.newDependencyType = '';
          
          this.$forceUpdate();
        },
        onDependencySelectChange(e) {
           if(this.newDependencyModel == -1) return;
     
          let selectedIndex = e.target.value;
           //Get selected as dependency to add
          let stateToUpdate = this.getStateByIndex(selectedIndex);     

          this.dependencySelectIsHidden = !this.isSensorDependency(stateToUpdate);

          this.$forceUpdate();
        }
    }
}
</script>
