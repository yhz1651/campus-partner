<script setup>
import {nextTick, reactive, ref} from "vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import config from "../../config";
import {useUserStore} from "@/stores/user";


const name = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const userStore = useUserStore()
const token = userStore.getBearerToken
const auths = userStore.getAuths

const state = reactive({
  tableData: [],
  form: {}
})

const valueHtml = ref('')  // 富文本内容

state.userOptions = []
request.get('/user').then(res => state.userOptions = res.data)
state.dynamicOptions = []
request.get('/dynamic').then(res => state.dynamicOptions = res.data)
state.commentOptions = []
request.get('/comment').then(res => state.commentOptions = res.data)


const multipleSelection = ref([])

// 批量删除
const handleSelectionChange = (val) => {
  multipleSelection.value = val
}

const confirmDelBatch = () => {
  if (!multipleSelection.value || !multipleSelection.value.length) {
    ElMessage.warning("请选择数据")
    return
  }
  const idArr = multipleSelection.value.map(v => v.id)
  request.post('/praise/del/batch', idArr).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()  // 刷新表格数据
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const load = () => {
  request.get('/praise/page', {
    params: {
      name: name.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }).then(res => {
    state.tableData = res.data.records
    total.value = res.data.total
  })
}
load()  // 调用 load方法拿到后台数据

const reset = () => {
  name.value = ''
  load()
}

const dialogFormVisible = ref(false)

const rules = reactive({
  name: [
    {required: true, message: '请输入名称', trigger: 'blur'},
  ]
})
const ruleFormRef = ref()

// 新增
const handleAdd = () => {
  dialogFormVisible.value = true
  nextTick(() => {
    ruleFormRef.value.resetFields()
    state.form = {}
    valueHtml.value = ''  // 富文本
  })
}

// 保存
const save = () => {
  ruleFormRef.value.validate(valid => {   // valid就是校验的结果
    if (valid) {
      state.form.content = valueHtml.value  // 富文本保存内容
      request.request({
        url: '/praise',
        method: state.form.id ? 'put' : 'post',
        data: state.form
      }).then(res => {
        if (res.code === '200') {
          ElMessage.success('保存成功')
          dialogFormVisible.value = false
          load()  // 刷新表格数据
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}

// 编辑
const handleEdit = (raw) => {
  dialogFormVisible.value = true
  nextTick(() => {
    ruleFormRef.value.resetFields()
    state.form = JSON.parse(JSON.stringify(raw))
    valueHtml.value = raw.content  // 富文本
  })
}

// 删除
const del = (id) => {
  request.delete('/praise/' + id).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()  // 刷新表格数据
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 导出接口
const exportData = () => {
  window.open(`http://${config.serverUrl}/praise/export`)
}


const handleImportSuccess = () => {
  // 刷新表格
  load()
  ElMessage.success("导入成功")
}

const handleFileUploadSuccess = (res) => {
  state.form.file = res.data
  ElMessage.success('上传成功')
}
const handleImgUploadSuccess = (res) => {
  state.form.img = res.data
  ElMessage.success('上传成功')
}

</script>

<template>
  <div>
<!--    <div>-->
<!--      <el-input v-model="name" placeholder="请输入模块id" class="w300"/>-->
<!--      <el-button type="primary" class="ml5" @click="load">-->
<!--        <el-icon style="vertical-align: middle">-->
<!--          <Search/>-->
<!--        </el-icon>-->
<!--        <span style="vertical-align: middle"> 搜索 </span>-->
<!--      </el-button>-->
<!--      <el-button type="warning" class="ml5" @click="reset">-->
<!--        <el-icon style="vertical-align: middle">-->
<!--          <RefreshLeft/>-->
<!--        </el-icon>-->
<!--        <span style="vertical-align: middle"> 重置 </span>-->
<!--      </el-button>-->

<!--    </div>-->

    <div style="margin: 10px 0">
<!--      <el-button type="success" @click="handleAdd" v-if="auths.includes('praise.add')">-->
<!--        <el-icon style="vertical-align: middle">-->
<!--          <Plus />-->
<!--        </el-icon>  <span style="vertical-align: middle"> 新增 </span>-->
<!--      </el-button>-->
      <el-upload
          v-if="auths.includes('praise.import')"
          class="ml5"
          :show-file-list="false"
          style="display: inline-block; position: relative; top: 3px"
          :action='`http://${config.serverUrl}/praise/import`'
          :on-success="handleImportSuccess"
          :headers="{ Authorization: token}"
      >
        <el-button type="primary">
          <el-icon style="vertical-align: middle">
            <Bottom/>
          </el-icon>
          <span style="vertical-align: middle"> 导入 </span>
        </el-button>
      </el-upload>
      <el-button type="primary" @click="exportData" class="ml5" v-if="auths.includes('praise.export')">
        <el-icon style="vertical-align: middle">
          <Top/>
        </el-icon>
        <span style="vertical-align: middle"> 导出 </span>
      </el-button>
      <el-popconfirm title="您确定删除吗？" @confirm="confirmDelBatch" v-if="auths.includes('praise.deleteBatch')">
        <template #reference>
          <el-button type="danger" style="margin-left: 5px">
            <el-icon style="vertical-align: middle">
              <Remove/>
            </el-icon>
            <span style="vertical-align: middle"> 批量删除 </span>
          </el-button>
        </template>
      </el-popconfirm>
    </div>

    <div style="margin: 10px 0">
      <el-table :data="state.tableData" stripe border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column type="index" label="编号" width="60"></el-table-column>
        <el-table-column prop="type" label="模块类型">
          <template #default="scope">
            <span v-if="scope.row.type === 'dynamic'">动态</span>
            <span v-if="scope.row.type === 'comment'">评论</span>
          </template>
        </el-table-column>
        <el-table-column label="模块标题（内容）">
          <template #default="scope">
            <span v-if="scope.row.type === 'dynamic'">{{
                state.dynamicOptions.find(v => v.id === scope.row.fid) ? state.dynamicOptions.find(v => v.id === scope.row.fid).name : ''
              }}
            </span>
            <span v-if="scope.row.type === 'comment'">{{
                state.commentOptions.find(v => v.id === scope.row.fid) ? state.commentOptions.find(v => v.id === scope.row.fid).content : ''
              }}
            </span>
          </template>
        </el-table-column>
<!--        <el-table-column prop="fid" label="模块id"></el-table-column>-->
<!--        <el-table-column prop="userId" label="点赞人id"></el-table-column>-->
        <el-table-column label="点赞人">
          <template #default="scope">
            <span v-if="scope.row.userId">{{
              state.userOptions.find(v => v.id === scope.row.userId) ? state.userOptions.find(v => v.id === scope.row.userId).name : ''
            }}</span>
          </template>
        </el-table-column>
<!--        <el-table-column prop="bepraiseId" label="被点赞人id"></el-table-column>-->
        <el-table-column label="被点赞人">
          <template #default="scope"><span v-if="scope.row.bepraiseId">{{
              state.userOptions.find(v => v.id === scope.row.bepraiseId) ? state.userOptions.find(v => v.id === scope.row.bepraiseId).name : ''
            }}</span></template>
        </el-table-column>
        <el-table-column prop="time" label="点赞时间"></el-table-column>

        <el-table-column label="操作" width="180">
          <template #default="scope">
<!--            <el-button type="primary" @click="handleEdit(scope.row)" v-if="auths.includes('praise.edit')">编辑</el-button>-->
            <el-popconfirm title="您确定删除吗？" @confirm="del(scope.row.id)" v-if="auths.includes('praise.delete')">
              <template #reference>
                <el-button type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div style="margin: 10px 0">
      <el-pagination
          @current-change="load"
          @size-change="load"
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          background
          :page-sizes="[2, 5, 10, 20]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
      />
    </div>

    <el-dialog v-model="dialogFormVisible" title="点赞信息" width="40%">
      <el-form ref="ruleFormRef" :rules="rules" :model="state.form" label-width="80px" style="padding: 0 20px"
               status-icon>
        <el-form-item prop="type" label="模块类型">
          <el-input v-model="state.form.type" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="fid" label="模块id">
          <el-input v-model="state.form.fid" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item prop="userId" label="点赞人">
          <el-select clearable v-model="state.form.userId" placeholder="请选择" style="width: 100%">
            <el-option v-for="item in state.userOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="bepraiseId" label="被点赞人">
          <el-select clearable v-model="state.form.bepraiseId" placeholder="请选择" style="width: 100%">
            <el-option v-for="item in state.userOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="time" label="点赞时间">
          <el-date-picker style="width: 100%" v-model="state.form.time" type="datetime"
                          value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择日期时间"></el-date-picker>
        </el-form-item>

      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="save">
          保存
        </el-button>
      </span>
      </template>
    </el-dialog>


  </div>
</template>