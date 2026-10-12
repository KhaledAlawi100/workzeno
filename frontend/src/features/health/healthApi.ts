import {apiGet} from "../../lib/api";

export async function getApiHealth(): Promise<{status: string}> {
    return await apiGet<{status: string}>("/health");
}
