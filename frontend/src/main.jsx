/**
 * Reactアプリのエントリーポイント。
 *
 * JSPホスト（main.jsp）に置かれた #root へReactをマウントし、bodyのdata属性から
 * contextPathを受け取ってAPIクライアントへ渡す。
 */
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import App from "./App.jsx";
import { configureApi } from "./api.js";
import "./styles.css";

const rootElement = document.getElementById("root");
configureApi(document.body.dataset.contextPath || "");

createRoot(rootElement).render(
  <StrictMode>
    <App />
  </StrictMode>
);
