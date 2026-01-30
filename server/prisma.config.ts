import "dotenv/config";
import { defineConfig } from "prisma/config";

export default defineConfig({
  schema: "prisma/schema.prisma", // Use aspas duplas aqui
  datasource: {
    url: process.env.DATABASE_URL,
  },
});