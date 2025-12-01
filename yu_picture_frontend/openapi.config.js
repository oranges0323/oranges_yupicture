import { generateService } from '@umijs/openapi'
//参考哪个接口文档去生成
generateService({
  requestLibPath: "import request from '@/request'",
  schemaPath: 'http://localhost:8123/api/v2/api-docs',
  serversPath: './src',
})
