// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** testDownloadFile GET /api/file/test/download */
export async function testDownloadFileUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.testDownloadFileUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<any>('/api/file/test/download', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** testDownloadFile PUT /api/file/test/download */
export async function testDownloadFileUsingPut(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.testDownloadFileUsingPUTParams,
  options?: { [key: string]: any }
) {
  return request<any>('/api/file/test/download', {
    method: 'PUT',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** testDownloadFile POST /api/file/test/download */
export async function testDownloadFileUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.testDownloadFileUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<any>('/api/file/test/download', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** testDownloadFile DELETE /api/file/test/download */
export async function testDownloadFileUsingDelete(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.testDownloadFileUsingDELETEParams,
  options?: { [key: string]: any }
) {
  return request<any>('/api/file/test/download', {
    method: 'DELETE',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** testDownloadFile PATCH /api/file/test/download */
export async function testDownloadFileUsingPatch(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.testDownloadFileUsingPATCHParams,
  options?: { [key: string]: any }
) {
  return request<any>('/api/file/test/download', {
    method: 'PATCH',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** testUploadFile GET /api/file/test/upload */
export async function testUploadFileUsingGet(
  body: {},
  file?: File,
  options?: { [key: string]: any }
) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, new Blob([JSON.stringify(item)], { type: 'application/json' }))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BaseResponseString_>('/api/file/test/upload', {
    method: 'GET',
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
}

/** testUploadFile PUT /api/file/test/upload */
export async function testUploadFileUsingPut(
  body: {},
  file?: File,
  options?: { [key: string]: any }
) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, new Blob([JSON.stringify(item)], { type: 'application/json' }))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BaseResponseString_>('/api/file/test/upload', {
    method: 'PUT',
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
}

/** testUploadFile POST /api/file/test/upload */
export async function testUploadFileUsingPost(
  body: {},
  file?: File,
  options?: { [key: string]: any }
) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, new Blob([JSON.stringify(item)], { type: 'application/json' }))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BaseResponseString_>('/api/file/test/upload', {
    method: 'POST',
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
}

/** testUploadFile DELETE /api/file/test/upload */
export async function testUploadFileUsingDelete(
  body: {},
  file?: File,
  options?: { [key: string]: any }
) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, new Blob([JSON.stringify(item)], { type: 'application/json' }))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BaseResponseString_>('/api/file/test/upload', {
    method: 'DELETE',
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
}

/** testUploadFile PATCH /api/file/test/upload */
export async function testUploadFileUsingPatch(
  body: {},
  file?: File,
  options?: { [key: string]: any }
) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, new Blob([JSON.stringify(item)], { type: 'application/json' }))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BaseResponseString_>('/api/file/test/upload', {
    method: 'PATCH',
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
}
