import { api } from "./api";
import type { DashboardResponse } from "@/types/dashboard";

export const dashboardApi = {
  consultar: (inicio: string, fim: string) =>
    api.get<DashboardResponse>(`/dashboard?inicio=${inicio}&fim=${fim}`),
};
