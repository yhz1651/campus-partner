<script setup>
import * as echarts from 'echarts'
import {onMounted, reactive} from "vue";
import request from "@/utils/request";

const state = reactive({
  dashboard: {}
})

onMounted(() => {
  let dynamicOption = {
    title: {
      text: "动态发布数量趋势图",
    },
    legend: {
      top: '5%',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    xAxis: {
      type: 'category',
      data: []
    },
    yAxis: {
      type: 'value'
    },
    grid: {
      top: '20%',
      height: '60%'
    },
    series: [
      {
        name: '数量',
        data: [],
        type: 'line',
      }
    ]
  }
  let topicOption = {
    title: {
      text: "话题热度排行图",
    },
    legend: {
      top: '5%',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    xAxis: {
      type: 'category',
      data: []
    },
    yAxis: {
      type: 'value'
    },
    grid: {
      top: '20%',
      height: '60%'
    },
    series: [
      {
        name: '热度',
        data: [],
        type: 'bar',
        smooth: true,
        itemStyle: {
          normal: {
            color: function (params) {
              //颜色列表长度需要设置大于柱的数量，颜色循环结束不会继续循环
              const colorList = ['#4169E1', '#7FFFD4', '#FFA500', '#d48265', '#91c7ae', '#749f83', '#ca8622', '#c1cfdc' ,'#eb3345', '#e5ec74','#4169E1', '#7FFFD4', '#FFA500', '#d48265', '#91c7ae', '#749f83', '#ca8622', '#c1cfdc' ,'#eb3345', '#e5ec74','#4169E1', '#7FFFD4', '#FFA500', '#d48265', '#91c7ae', '#749f83', '#ca8622', '#c1cfdc' ,'#eb3345', '#e5ec74'];
              return colorList[params.dataIndex]
            }
          }
        },
      },
    ]
  }
  let userOption = {
    title: {
      text: "用户地区分布图",
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: '10%',
      left: 'center'
    },
    series: [
      {
        name: '地区',
        type: 'pie',
        radius: ['40%', '60%'],
        center: ['50%', '55%'],
        avoidLabelOverlap: false,
        label: {
          show: true,
        },
        data: []
      }
    ]
  }
  let sexOption = {
    title: {
      text: "用户性别分布图",
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      top: '10%',
      left: 'center'
    },
    series: [
      {
        name: '性别',
        type: 'pie',
        radius: '50%',
        avoidLabelOverlap: false,
        label: {
          show: true,
        },
        data: [],
        itemStyle: {
          normal: {
            color: function (params) {
              //颜色列表长度需要设置大于柱的数量，颜色循环结束不会继续循环
              const colorList = ['#FF1493', '#4169E1'];
              return colorList[params.dataIndex]
            }
          }
        },
      }
    ]
  }
  let dynamicChart = echarts.init(document.getElementById("dynamic"))
  let topicChart = echarts.init(document.getElementById("topic"))
  let userChart = echarts.init(document.getElementById("user"))
  let sexChart = echarts.init(document.getElementById("sex"))

  // 用户地点分布饼图
  request.get('/echarts/users').then(res => {
    userOption.series[0].data = res.data
    userChart.setOption(userOption)
  })

  // 用户性别分布饼图
  request.get('/echarts/sex').then(res => {
    sexOption.series[0].data = res.data
    sexChart.setOption(sexOption)
  })

  // 动态折线图
  request.get('/echarts/dynamicCount').then(res => {
    dynamicOption.xAxis.data = res.data.map(v => v.name)
    dynamicOption.series[0].data = res.data.map(v => v.value)
    // dynamicOption.series[1].data = res.data.map(v => v.value)
    dynamicChart.setOption(dynamicOption)
  })

  // 话题柱状图
  request.get('/echarts/topics').then(res => {
    topicOption.xAxis.data = res.data.map(v => v.name)
    topicOption.series[0].data = res.data.map(v => v.hot)
    topicChart.setOption(topicOption)
  })

  request.get('/echarts/dashboard').then(res => {
    state.dashboard = res.data
  })
})



</script>

<template>
  <div>
    <div style="text-align: center">
      <el-row :gutter="10">
        <el-col :span="3">
          <el-card style="height: 100px;">
            <div style="color: #888">用户数量</div>
            <div style="font-size: 24px; font-weight: bold">{{ state.dashboard.users }}</div>
          </el-card>
        </el-col>
        <el-col :span="3">
          <el-card style="height: 100px;">
            <div style="color: #888">角色数量</div>
            <div style="font-size: 24px; font-weight: bold">{{ state.dashboard.roles }}</div>
          </el-card>
        </el-col>
        <el-col :span="3">
          <el-card style="height: 100px;">
            <div style="color: #888">动态数量</div>
            <div style="font-size: 24px; font-weight: bold">{{ state.dashboard.dynamics }}</div>
          </el-card>
        </el-col>
        <el-col :span="3">
          <el-card style="height: 100px;">
            <div style="color: #888">活动数量</div>
            <div style="font-size: 24px; font-weight: bold">{{ state.dashboard.activities }}</div>
          </el-card>
        </el-col>
        <el-col :span="3">
          <el-card style="height: 100px;">
            <div style="color: #888">话题数量</div>
            <div style="font-size: 24px; font-weight: bold">{{ state.dashboard.topics }}</div>
          </el-card>
        </el-col>
        <el-col :span="3">
          <el-card style="height: 100px;">
            <div style="color: #888">聊天数量</div>
            <div style="font-size: 24px; font-weight: bold">{{ state.dashboard.ims }}</div>
          </el-card>
        </el-col>
        <el-col :span="3">
          <el-card style="height: 100px;">
            <div style="color: #888">后台公告数量</div>
            <div style="font-size: 24px; font-weight: bold">{{ state.dashboard.notices }}</div>
          </el-card>
        </el-col>
        <el-col :span="3">
          <el-card style="height: 100px;">
            <div style="color: #888">前台公告数量</div>
            <div style="font-size: 24px; font-weight: bold">{{ state.dashboard.information }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-card style="margin: 20px 0">
      <el-row :gutter="10">
        <el-col :span="12">
          <div style="width:100%; height: 500px" id="dynamic"></div>
        </el-col>
        <el-col :span="12">
          <div style="width:100%; height: 500px" id="topic"></div>
        </el-col>
      </el-row>
    </el-card>
    <el-card style="margin: 20px 0">
      <el-row :gutter="10">
        <el-col :span="12">
          <div style="width:100%; height: 500px" id="user"></div>
        </el-col>
        <el-col :span="12">
          <div style="width:100%; height: 500px" id="sex"></div>
        </el-col>
      </el-row>
    </el-card>


  </div>
</template>
