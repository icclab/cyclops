.. Cyclops documentation master file, created by
   sphinx-quickstart on Tue Sep 26 15:54:15 2017.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Welcome to Cyclops's documentation!
===================================
Cyclops is an open source, community driven project led by Cloud Accounting 
and Billing (CAB) initiative @ SPLAB, part of InIT - ZHAW, for creating a 
flexible accounting and billing framework for IT services. Cyclops has been 
specifically designed keeping requirements of popular cloud native 
applications, platforms and services in mind. Widely used platforms such as 
OpenStack, CloudStack, Apache Hadoop, etc. are already supporting, meaning - 
these can be billed out of box through Cyclops framework via appropriate 
collectors.

.. figure:: v3.png
    :width: 800px
    :align: center
    :alt: Cyclops v3 architecture
    :figclass: align-center

    Figure 1: Cyclops framework architecture (v3.0)

This manual covers only installation and administration of Cyclops 
installation.

.. seealso::

    You may want to read `Cyclops's Developer's guide`__ (WiKi) -- the first 
    bit, at least -- to get an idea of the concepts.

    __ https://github.com/icclab/cyclops/wiki


Contents:

.. toctree::
   :maxdepth: 2
   :caption: Contents:

   build.rst
   prereq.rst
   setup_udr.rst
   setup_cdr.rst
   setup_billing.rst
   setup_coin.rst
   manage.rst


Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`
