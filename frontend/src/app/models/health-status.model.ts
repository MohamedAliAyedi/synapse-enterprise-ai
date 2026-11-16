export interface HealthStatus {
  status: string;
  components: {
    [key: string]: {
      status: string;
      details?: any;
    };
  };
}
