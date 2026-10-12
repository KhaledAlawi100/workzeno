const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

if(!API_BASE_URL) {
    throw new Error("VITE_API_BASE_URL is not defined in the environment variables.");
};

export async function apiGet<T>(path: string): Promise<T> {

    const response = await fetch(`${API_BASE_URL}${path}`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        },
    });

    if (!response.ok) {
        throw new Error(`API request failed with status ${response.status}`);
    }
    const data = await response.json();

    return data as Promise<T>;

}