/* Expense Tracker — stores expenses in localStorage, weeks run Monday → Sunday. */
(function () {
  "use strict";

  var STORAGE_KEY = "expense_tracker_expenses";
  var CATEGORIES = ["Food", "Transport", "Shopping", "Bills", "Entertainment", "Other"];
  var CATEGORY_EMOJI = {
    Food: "🍔",
    Transport: "🚕",
    Shopping: "🛍️",
    Bills: "🧾",
    Entertainment: "🎬",
    Other: "📦"
  };

  // 0 = current week; positive = future weeks, negative = past weeks
  var weekOffset = 0;

  /* ---------- storage ---------- */
  function loadExpenses() {
    try {
      var raw = localStorage.getItem(STORAGE_KEY);
      var list = raw ? JSON.parse(raw) : [];
      return Array.isArray(list) ? list : [];
    } catch (e) {
      return [];
    }
  }

  function saveExpenses(list) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
  }

  function addExpense(expense) {
    var list = loadExpenses();
    list.push(expense);
    saveExpenses(list);
  }

  function deleteExpense(id) {
    var list = loadExpenses().filter(function (e) { return e.id !== id; });
    saveExpenses(list);
  }

  /* ---------- week helpers (Monday → Sunday) ---------- */
  function mondayOf(date) {
    var d = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    var day = d.getDay(); // 0 = Sunday
    var diff = (day + 6) % 7; // days since Monday
    d.setDate(d.getDate() - diff);
    d.setHours(0, 0, 0, 0);
    return d;
  }

  function weekRange() {
    var base = new Date();
    base.setDate(base.getDate() + weekOffset * 7);
    var start = mondayOf(base);
    var end = new Date(start);
    end.setDate(end.getDate() + 6);
    return { start: start, end: end };
  }

  function inCurrentWeek(dateStr) {
    var range = weekRange();
    var d = new Date(dateStr + "T00:00:00");
    return d >= range.start && d.getTime() <= range.end.getTime() + 86399999;
  }

  function weekExpenses() {
    return loadExpenses()
      .filter(function (e) { return inCurrentWeek(e.date); })
      .sort(function (a, b) { return b.date.localeCompare(a.date); });
  }

  /* ---------- formatting ---------- */
  function fmt(amount) {
    return "₹" + Number(amount).toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  function fmtDate(dateStr) {
    var d = new Date(dateStr + "T00:00:00");
    return d.toLocaleDateString("en-IN", { day: "numeric", month: "short" });
  }

  function weekLabel() {
    var r = weekRange();
    var opts = { day: "numeric", month: "short" };
    var label = r.start.toLocaleDateString("en-IN", opts) + " – " + r.end.toLocaleDateString("en-IN", opts);
    if (weekOffset === 0) return "This week · " + label;
    if (weekOffset === -1) return "Last week · " + label;
    if (weekOffset === 1) return "Next week · " + label;
    return label;
  }

  /* ---------- rendering ---------- */
  function renderList() {
    var list = weekExpenses();
    var ul = document.getElementById("expense-list");
    var empty = document.getElementById("expense-empty");
    ul.innerHTML = "";

    list.forEach(function (e) {
      var li = document.createElement("li");

      var meta = document.createElement("div");
      meta.className = "expense-meta";
      var cat = document.createElement("span");
      cat.className = "expense-cat";
      cat.textContent = (CATEGORY_EMOJI[e.category] || "📦") + " " + e.category;
      var desc = document.createElement("span");
      desc.className = "expense-desc";
      desc.textContent = (e.description || "—") + " · " + fmtDate(e.date);
      meta.appendChild(cat);
      meta.appendChild(desc);

      var right = document.createElement("div");
      right.className = "expense-right";
      var amt = document.createElement("span");
      amt.className = "expense-amount";
      amt.textContent = fmt(e.amount);
      var del = document.createElement("button");
      del.className = "btn danger";
      del.type = "button";
      del.textContent = "🗑️";
      del.title = "Delete";
      del.setAttribute("aria-label", "Delete expense");
      del.addEventListener("click", function () {
        deleteExpense(e.id);
        renderAll();
      });
      right.appendChild(amt);
      right.appendChild(del);

      li.appendChild(meta);
      li.appendChild(right);
      ul.appendChild(li);
    });

    empty.hidden = list.length > 0;
    document.getElementById("week-label-mini").textContent = weekLabel();
  }

  function renderSummary() {
    var list = weekExpenses();
    var total = list.reduce(function (sum, e) { return sum + Number(e.amount); }, 0);
    document.getElementById("total-spent").textContent = fmt(total);
    document.getElementById("week-label").textContent = weekLabel();

    var byCat = {};
    list.forEach(function (e) {
      byCat[e.category] = (byCat[e.category] || 0) + Number(e.amount);
    });

    var rows = Object.keys(byCat)
      .map(function (cat) { return { cat: cat, total: byCat[cat] }; })
      .sort(function (a, b) { return b.total - a.total; });

    var box = document.getElementById("breakdown");
    var empty = document.getElementById("breakdown-empty");
    box.innerHTML = "";

    rows.forEach(function (row, idx) {
      var pct = total > 0 ? Math.round((row.total / total) * 100) : 0;

      var wrap = document.createElement("div");
      wrap.className = "cat-row";

      var head = document.createElement("div");
      head.className = "cat-head";
      var name = document.createElement("span");
      name.className = "cat-name";
      name.textContent = (CATEGORY_EMOJI[row.cat] || "📦") + " " + row.cat;
      if (idx === 0 && rows.length > 1) {
        var badge = document.createElement("span");
        badge.className = "top-badge";
        badge.textContent = "top spend";
        name.appendChild(badge);
      }
      var vals = document.createElement("span");
      vals.className = "cat-vals";
      vals.textContent = fmt(row.total) + " · " + pct + "%";
      head.appendChild(name);
      head.appendChild(vals);

      var bar = document.createElement("div");
      bar.className = "bar";
      var fill = document.createElement("div");
      fill.className = "bar-fill";
      fill.style.width = pct + "%";
      bar.appendChild(fill);

      wrap.appendChild(head);
      wrap.appendChild(bar);
      box.appendChild(wrap);
    });

    empty.hidden = rows.length > 0;
  }

  function renderAll() {
    renderList();
    renderSummary();
  }

  /* ---------- tabs ---------- */
  function switchView(name) {
    document.querySelectorAll(".tab").forEach(function (t) {
      t.classList.toggle("active", t.dataset.view === name);
    });
    document.querySelectorAll(".view").forEach(function (v) {
      v.classList.toggle("active", v.id === "view-" + name);
    });
    if (name === "summary") renderSummary();
  }

  /* ---------- events ---------- */
  document.querySelectorAll(".tab").forEach(function (t) {
    t.addEventListener("click", function () { switchView(t.dataset.view); });
  });

  function shiftWeek(delta) {
    weekOffset += delta;
    renderAll();
  }
  document.getElementById("prev-week").addEventListener("click", function () { shiftWeek(-1); });
  document.getElementById("next-week").addEventListener("click", function () { shiftWeek(1); });
  document.getElementById("prev-week-mini").addEventListener("click", function () { shiftWeek(-1); });
  document.getElementById("next-week-mini").addEventListener("click", function () { shiftWeek(1); });

  document.getElementById("expense-form").addEventListener("submit", function (ev) {
    ev.preventDefault();
    var errorEl = document.getElementById("form-error");
    errorEl.hidden = true;

    var amount = parseFloat(document.getElementById("amount").value);
    var description = document.getElementById("description").value.trim();
    var category = document.getElementById("category").value;
    var date = document.getElementById("date").value;

    if (!amount || isNaN(amount) || amount <= 0) {
      return showError("Please enter an amount greater than 0.");
    }
    if (!category || CATEGORIES.indexOf(category) === -1) {
      return showError("Please select a category.");
    }
    if (!date) {
      return showError("Please pick a date.");
    }

    addExpense({
      id: Date.now().toString(36) + Math.random().toString(36).slice(2, 7),
      amount: Math.round(amount * 100) / 100,
      description: description,
      category: category,
      date: date
    });

    document.getElementById("expense-form").reset();
    document.getElementById("date").value = todayISO();
    renderAll();
    switchView("summary");
  });

  function showError(msg) {
    var errorEl = document.getElementById("form-error");
    errorEl.textContent = msg;
    errorEl.hidden = false;
  }

  function todayISO() {
    var d = new Date();
    var m = String(d.getMonth() + 1).padStart(2, "0");
    var day = String(d.getDate()).padStart(2, "0");
    return d.getFullYear() + "-" + m + "-" + day;
  }

  /* ---------- init ---------- */
  document.getElementById("date").value = todayISO();
  renderAll();
})();
