# Hibernate + Eclipse Collections (Naive Demo)

> **Disclaimer:** This project is intended **for demonstration purposes only**.  
> It shows a *naive* approach to using [Eclipse Collections](https://www.eclipse.org/collections/) together with [Hibernate ORM](https://hibernate.org/).  
> **Do not** copy or replicate this code in production — it is **not a recommended or supported pattern** for real-world applications.

---

## Overview

This demo explores one means of enabling the use of Eclipse Collections with Hibernate

Hibernate was designed to work with standard Java collections (`List`, `Set`, `Map`)

This repository exists to show how in some cases, this is achievable by using ListAdapter to wrap the proxied Hibernate 
collections. It provides only one example of using a MutableList within an `@Entity`
---