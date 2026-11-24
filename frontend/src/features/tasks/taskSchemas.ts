import { z } from "zod";

/**
 * Returns today's date in YYYY-MM-DD format using local time.
 */
function getTodayDate(): string {
  const today = new Date();

  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

export const taskFormSchema = z.object({
  title: z
    .string()
    .trim()
    .min(1, "Task title is required")
    .max(200, "Task title must not exceed 200 characters"),

  description: z
    .string()
    .trim()
    .max(2000, "Description must not exceed 2000 characters"),

  priority: z.enum(["LOW", "MEDIUM", "HIGH", "URGENT"]),

  dueDate: z.string().refine(
    (value) => {
      if (value === "") return true;

      // Validate the YYYY-MM-DD format.
      if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
        return false;
      }

      // Reject dates earlier than today.
      return value >= getTodayDate();
    },
    {
      message: "Due date cannot be in the past",
    },
  ),
});

export type TaskFormValues = z.infer<typeof taskFormSchema>;
