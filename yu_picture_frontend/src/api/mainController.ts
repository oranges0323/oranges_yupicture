// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** feedback POST /api/feedback */
export async function feedbackUsingPost(
  body: Record<string, any>,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseString_>('/api/feedback', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** health GET /api/health */
export async function healthUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseString_>('/api/health', {
    method: 'GET',
    ...(options || {}),
  })
}
