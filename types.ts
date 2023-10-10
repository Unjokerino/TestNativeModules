export interface PostParams {
  type: 'POST';
  body?: Record<string, string | number | boolean>;
  headers: Record<string, string>;
}

export interface GetParams {
  type: 'GET';
  headers: Record<string, string>;
}

export type RequestParams = PostParams | GetParams;

export interface SuccessResponse {
  type: 'success';
  data?: string;
  statusCode: number;
}

export interface ErrorResponse {
  type: 'error';
  statusCode: number;
  error: string;
}

export type Response = SuccessResponse | ErrorResponse;
