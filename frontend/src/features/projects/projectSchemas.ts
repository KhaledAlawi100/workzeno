import { z } from "zod";

export const createProjectSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, "Project name is required")
    .max(120, "Project name must not exceed 120 characters"),

  description: z
    .string()
    .trim()
    .max(2000, "Description must not exceed 2000 characters"),
});

export type CreateProjectFormValues = z.infer<typeof createProjectSchema>;
